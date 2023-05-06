package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.UpdateCourseDto;
import com.xuecheng.content.model.po.*;
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

    @Resource
    private CourseTeacherMapper courseTeacherMapper;

    @Resource
    private TeachplanMapper teachplanMapper;

    @Resource
    private TeachplanMediaMapper teachplanMediaMapper;

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

    /**
     * 获取课程信息
     *
     * @param id
     * @return
     */
    @Override
    public CourseBaseInfoDto getCourseById(Long id) {
        return getCourseBaseInfo(id);
    }

    /**
     * 修改课程信息
     *
     * @param companyId
     * @param updateCourseDto
     * @return
     */
    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBase(Long companyId, UpdateCourseDto updateCourseDto) {
        CourseBase base = courseBaseMapper.selectById(updateCourseDto.getId());
        if (base == null) {
            XueChengPlusException.cast(CommonError.QUERY_NULL);
        }
        if (companyId == null || !companyId.equals(base.getCompanyId())) {
            XueChengPlusException.cast(CommonError.PARAMS_ERROR);
        }
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(updateCourseDto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        //审核状态默认为未提交
        courseBase.setAuditStatus("202002");
        //发布状态为未发布
        courseBase.setStatus("203001");
        courseBaseMapper.updateById(courseBase);
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(updateCourseDto, courseMarket);
        courseMarket.setId(courseBase.getId());
        saveCourseMarket(courseMarket);
        return getCourseBaseInfo(courseBase.getId());
    }

    /**
     * 删除课程
     *
     * @param courseId
     */
    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            //已发布课程不能删除
            XueChengPlusException.cast("课程不存在");
        }
        if (courseBase.getStatus().equals("203002")) {
            //已发布课程不能删除
            XueChengPlusException.cast("课程已发布不能删除");
        }
        courseBaseMapper.deleteById(courseId);
        courseMarketMapper.deleteById(courseId);
        deleteCourseRelation(courseId, CourseTeacher.class, courseTeacherMapper);
        deleteCourseRelation(courseId, Teachplan.class, teachplanMapper);
        deleteCourseRelation(courseId, TeachplanMedia.class, teachplanMediaMapper);
    }

    //删除课程相关的信息
    public <T> void deleteCourseRelation(Long courseId, Class<T> clazz, BaseMapper<T> baseMapper) {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", courseId);
        baseMapper.delete(queryWrapper);
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
        if (courseMarket == null) {
            return null;
        }
        //组装在一起
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        //通过courseCategoryMapper查询分类信息，将分类名称放在courseBaseInfoDto对象
        String st = courseBase.getSt();
        String mt = courseBase.getMt();
        courseBaseInfoDto.setStName(courseCategoryMapper.getCourseCategoryNameById(st));
        courseBaseInfoDto.setMtName(courseCategoryMapper.getCourseCategoryNameById(mt));
        return courseBaseInfoDto;

    }

    //单独写一个方法保存营销信息，逻辑：存在则更新，不存在则添加
    private int saveCourseMarket(CourseMarket courseMarket) {
        //参数的合法性校验
        String charge = courseMarket.getCharge();
        if (StringUtils.isEmpty(charge)) {
            XueChengPlusException.cast(CommonError.REQUEST_NULL);
        }
        //如果课程收费，价格没有填写也需要抛出异常
        if (charge.equals("201001")) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice() <= 0) {
                XueChengPlusException.cast(CommonError.PARAMS_ERROR);
            }
        }
        //从数据库查询营销信息,存在则更新，不存在则添加
        Long id = courseMarket.getId();//主键
        CourseMarket courseMarketDataBase = courseMarketMapper.selectById(id);
        if (courseMarketDataBase == null) {
            //插入数据库
            return courseMarketMapper.insert(courseMarket);
        } else {
            return courseMarketMapper.updateById(courseMarket);
        }

    }
}
