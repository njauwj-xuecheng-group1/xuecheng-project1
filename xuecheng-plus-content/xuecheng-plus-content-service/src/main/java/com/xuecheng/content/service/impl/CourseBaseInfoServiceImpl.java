package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/2 14:59
 * @explain:
 */
@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Resource
    private CourseMarketMapper courseMarketMapper;

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    /**
     * 分页条件查询课程列表
     *
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //审核状态
        String auditStatus = queryCourseParamsDto.getAuditStatus();
        queryWrapper.eq(StringUtils.isNotBlank(auditStatus), CourseBase::getAuditStatus, auditStatus);
        //课程名字
        String courseName = queryCourseParamsDto.getCourseName();
        queryWrapper.like(StringUtils.isNotBlank(courseName), CourseBase::getName, courseName);
        //发布状态
        String publishStatus = queryCourseParamsDto.getPublishStatus();
        queryWrapper.eq(StringUtils.isNotBlank(publishStatus), CourseBase::getStatus, publishStatus);
        //分页
        Long pageNo = pageParams.getPageNo();
        Long pageSize = pageParams.getPageSize();
        Page<CourseBase> page = new Page<>(pageNo, pageSize);
        Page<CourseBase> result = courseBaseMapper.selectPage(page, queryWrapper);
        List<CourseBase> courseBaseList = result.getRecords();
        long total = result.getTotal();
        return new PageResult<>(courseBaseList, total, pageNo, pageSize);
    }

    /**
     * 新增课程
     *
     * @param companyId
     * @param dto
     * @return
     */
    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        //参数的合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            XueChengPlusException.cast(CommonError.REQUEST_NULL);
        }


        if (StringUtils.isBlank(dto.getMt())) {
            XueChengPlusException.cast(CommonError.REQUEST_NULL);
        }

        if (StringUtils.isBlank(dto.getSt())) {
            XueChengPlusException.cast(CommonError.REQUEST_NULL);
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            XueChengPlusException.cast(CommonError.REQUEST_NULL);
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            XueChengPlusException.cast(CommonError.REQUEST_NULL);
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            XueChengPlusException.cast(CommonError.REQUEST_NULL);
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            XueChengPlusException.cast(CommonError.REQUEST_NULL);
        }

        //向课程基本信息表course_base写入数据
        CourseBase courseBaseNew = new CourseBase();
        BeanUtils.copyProperties(dto, courseBaseNew);//只要属性名称一致就可以拷贝
        courseBaseNew.setCompanyId(companyId);
        courseBaseNew.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交
        courseBaseNew.setAuditStatus("202002");
        //发布状态为未发布
        courseBaseNew.setStatus("203001");
        //插入数据库
        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }

        //向课程营销系courese_market写入数据
        CourseMarket courseMarketNew = new CourseMarket();
        //将页面输入的数据拷贝到courseMarketNew
        BeanUtils.copyProperties(dto, courseMarketNew);
        //课程的id
        Long courseId = courseBaseNew.getId();
        courseMarketNew.setId(courseId);
        //保存营销信息
        saveCourseMarket(courseMarketNew);
        //从数据库查询课程的详细信息，包括两部分
        return getCourseBaseInfo(courseId);
    }

    //查询课程信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId) {

        //从课程基本信息表查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            return null;
        }
        //从课程营销表查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        //组装在一起
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }

        //通过courseCategoryMapper查询分类信息，将分类名称放在courseBaseInfoDto对象
        String st = courseBase.getSt();
        String mt = courseBase.getMt();
        courseBaseInfoDto.setStName(courseCategoryMapper.getCourseCategoryNameById(st));
        courseBaseInfoDto.setStName(courseCategoryMapper.getCourseCategoryNameById(mt));
        return courseBaseInfoDto;

    }

    //单独写一个方法保存营销信息，逻辑：存在则更新，不存在则添加
    private int saveCourseMarket(CourseMarket courseMarketNew) {

        //参数的合法性校验
        String charge = courseMarketNew.getCharge();
        if (StringUtils.isEmpty(charge)) {
            XueChengPlusException.cast(CommonError.REQUEST_NULL);
        }
        //如果课程收费，价格没有填写也需要抛出异常
        if (charge.equals("201001")) {
            if (courseMarketNew.getPrice() == null || courseMarketNew.getPrice() <= 0) {
                XueChengPlusException.cast(CommonError.PARAMS_ERROR);
            }
        }

        //从数据库查询营销信息,存在则更新，不存在则添加
        Long id = courseMarketNew.getId();//主键
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if (courseMarket == null) {
            //插入数据库
            return courseMarketMapper.insert(courseMarketNew);
        } else {
            //将courseMarketNew拷贝到courseMarket
            BeanUtils.copyProperties(courseMarketNew, courseMarket);
            courseMarket.setId(courseMarketNew.getId());
            //更新
            return courseMarketMapper.updateById(courseMarket);
        }


    }
}
