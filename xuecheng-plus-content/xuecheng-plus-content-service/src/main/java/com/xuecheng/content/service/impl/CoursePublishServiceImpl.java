package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanTreeDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.*;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/12 13:17
 * @explain:
 */
@Service
@Slf4j
public class CoursePublishServiceImpl implements CoursePublishService {

    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @Resource
    private TeachPlanService teachPlanService;

    @Resource
    private CourseTeacherService courseTeacherService;

    @Resource
    private CourseMarketService courseMarketService;

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Resource
    private CoursePublishPreMapper coursePublishPreMapper;

    @Resource
    private CoursePublishMapper coursePublishMapper;

    @Resource
    private MqMessageService mqMessageService;

    @Resource
    private MediaServiceClient mediaServiceClient;


    @Override
    public CoursePreviewDto preview(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseById(courseId);
        List<TeachPlanTreeDto> teachPlans = teachPlanService.getTeachPlanTree(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachPlans);

        return coursePreviewDto;
    }

    /**
     * 课程提交审核
     *
     * @param courseId [{"code":"202001","desc":"审核未通过"},{"code":"202002","desc":"未提交"},{"code":"202003","desc":"已提交"},{"code":"202004","desc":"审核通过"}]
     */
    @Transactional
    @Override
    public void courseAudit(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在");
        }
        if (!companyId.equals(courseBase.getCompanyId())) {
            XueChengPlusException.cast("只允许提交自己机构的课程");
        }
        String status = courseBase.getAuditStatus();
        if ("202003".equals(status)) {
            XueChengPlusException.cast("课程已提交");
        }
        if (StringUtils.isBlank(courseBase.getPic())) {
            XueChengPlusException.cast("请提交课程图片");
        }
        courseBase.setAuditStatus("202003");
        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseById(courseId);
        courseBaseInfoDto.setAuditStatus("202003");
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfoDto, coursePublishPre);
        coursePublishPre.setStatus("202003");
        List<TeachPlanTreeDto> teachPlanTree = teachPlanService.getTeachPlanTree(courseId);
        if (teachPlanTree.isEmpty()) {
            XueChengPlusException.cast("课程没有计划");
        }
        String teachPlanJson = JSON.toJSONString(teachPlanTree);
        coursePublishPre.setTeachplan(teachPlanJson);
        List<CourseTeacher> courseTeachers = courseTeacherService.getCourseTeachers(courseId);
        if (courseTeachers.isEmpty()) {
            XueChengPlusException.cast("课程没有老师");
        }
        String courseTeachersJson = JSON.toJSONString(courseTeachers);
        coursePublishPre.setTeachers(courseTeachersJson);
        CourseMarket courseMarket = courseMarketService.getCourseMarketById(courseId);
        if (courseMarket == null) {
            XueChengPlusException.cast("课程没有营销信息");
        }
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        coursePublishPre.setCreateDate(LocalDateTime.now());
        CoursePublishPre selectObj = coursePublishPreMapper.selectById(courseId);
        if (selectObj == null) {
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            coursePublishPreMapper.updateById(coursePublishPre);
        }
        courseBaseMapper.updateById(courseBase);

    }

    /**
     * 课程发布
     *
     * @param companyId
     * @param courseId
     */
    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("课程未提交审核");
        }
        if (!coursePublishPre.getStatus().equals("202004")) {
            XueChengPlusException.cast("课程未通过审核，不能发布");
        }
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        //向课程发布表写数据
        CoursePublish publish = coursePublishMapper.selectById(courseId);
        if (publish == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }
        //向消息表写入数据
        saveCoursePublishMessage(courseId);
        // 删除预发布表的数据
        coursePublishPreMapper.deleteById(courseId);
    }

    /**
     * 生成课程静态页面
     *
     * @param courseId
     * @return
     */
    @Override
    public File generateStaticHtml(Long courseId) {

        File tempFile = null;
        try {
            Configuration configuration = new Configuration(Configuration.getVersion());
            //拿到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            //指定模板的目录
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //指定编码
            configuration.setDefaultEncoding("utf-8");
            //得到模板
            Template template = configuration.getTemplate("course_template.ftl");
            //准备数据
            CoursePreviewDto coursePreviewInfo = preview(courseId);
            HashMap<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);
            //Template template 模板, Object model 数据
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //输入流
            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
            //输出文件
            tempFile = File.createTempFile("coursePublish", ".html");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            //使用流将html写入文件
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("生成课程静态页面出错，课程id为{}", courseId);
            XueChengPlusException.cast("生成课程静态页面出错");
        }
        return tempFile;

    }

    /**
     * 上传静态页面到Minio
     */
    @Override
    public void uploadHtmlToMinio(File file, Long courseId) {
        //将file转成MultipartFile
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        try {
            //远程调用得到返回值
            String upload = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
            if (upload == null) {
                log.debug("走了降级逻辑");
            }
        } catch (Exception e) {
            log.error("上传静态页面至Minio出错，课程id为{}", courseId);
            XueChengPlusException.cast("上传静态页面至Minio出错");
        }
    }


    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }

}
