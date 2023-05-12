package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanTreeDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/12 13:17
 * @explain:
 */
@Service
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
}
