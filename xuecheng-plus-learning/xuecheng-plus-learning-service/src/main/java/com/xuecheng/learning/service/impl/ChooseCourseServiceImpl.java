package com.xuecheng.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.service.ChooseCourseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/22 15:04
 * @explain:
 */
@Service
public class ChooseCourseServiceImpl implements ChooseCourseService {


    @Resource
    private ContentServiceClient contentServiceClient;

    @Resource
    private XcChooseCourseMapper chooseCourseMapper;

    @Resource
    private XcCourseTablesMapper courseTablesMapper;

    /**
     * 进行选课
     *
     * @param courseId
     * @param userId
     * @return
     */
    @Override
    @Transactional
    public XcChooseCourseDto addChooseCourse(Long courseId, String userId) {
        //查询发布课程
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish == null) {
            XueChengPlusException.cast("课程不存在");
        }
        String charge = coursepublish.getCharge();
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        XcChooseCourse xcChooseCourse = null;
        if ("201000".equals(charge)) {
            //免费课程选课
            xcChooseCourse = addFreeCourse(coursepublish, userId);
            //写入到我的课程
            addMyCourse(xcChooseCourse);
        } else {
            //收费课程选课
            xcChooseCourse = addChargeCourse(coursepublish, userId);
        }
        XcCourseTablesDto xcCourseTablesDto = getLearnstatus(courseId, userId);
        BeanUtils.copyProperties(xcChooseCourse, xcChooseCourseDto);
        xcChooseCourseDto.setLearnStatus(xcCourseTablesDto.getLearnStatus());
        return xcChooseCourseDto;
    }

    @Override
    public XcCourseTablesDto getLearnstatus(Long courseId, String userId) {
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcCourseTables::getCourseId, courseId).eq(XcCourseTables::getUserId, userId);
        XcCourseTables xcCourseTables = courseTablesMapper.selectOne(queryWrapper);
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        if (xcCourseTables == null) {
            xcCourseTablesDto.setLearnStatus("702002");
            return xcCourseTablesDto;
        }
        LocalDateTime validtimeEnd = xcCourseTables.getValidtimeEnd();
        if (validtimeEnd.isBefore(LocalDateTime.now())) {
            //已过期
            BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
            xcCourseTablesDto.setLearnStatus("702003");
            return xcCourseTablesDto;
        }
        BeanUtils.copyProperties(xcCourseTables, xcCourseTablesDto);
        xcCourseTablesDto.setLearnStatus("702001");
        return xcCourseTablesDto;
    }


    private void addMyCourse(XcChooseCourse xcChooseCourse) {
        if (!"701001".equals(xcChooseCourse.getStatus())) {
            XueChengPlusException.cast("选课失败");
        }
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcCourseTables::getChooseCourseId, xcChooseCourse.getId());
        List<XcCourseTables> xcCourseTables = courseTablesMapper.selectList(queryWrapper);
        if (!xcCourseTables.isEmpty()) {
            //已添加我的课程当中无需重复添加
            return;
        }
        XcCourseTables myCourse = new XcCourseTables();
        myCourse.setChooseCourseId(xcChooseCourse.getId());
        myCourse.setUserId(xcChooseCourse.getUserId());
        myCourse.setCourseId(xcChooseCourse.getCourseId());
        myCourse.setCompanyId(xcChooseCourse.getCompanyId());
        myCourse.setCourseName(xcChooseCourse.getCourseName());
        myCourse.setCourseType(xcChooseCourse.getOrderType());
        myCourse.setCreateDate(LocalDateTime.now());
        myCourse.setValidtimeStart(LocalDateTime.now());
        myCourse.setValidtimeEnd(LocalDateTime.now().plusDays(xcChooseCourse.getValidDays()));
        courseTablesMapper.insert(myCourse);
    }

    private XcChooseCourse addFreeCourse(CoursePublish coursepublish, String userId) {
        return addCourse(coursepublish, userId, "700001", "701001");
    }

    private XcChooseCourse addChargeCourse(CoursePublish coursepublish, String userId) {
        return addCourse(coursepublish, userId, "700002", "701002");
    }


    private XcChooseCourse addCourse(CoursePublish coursepublish, String userId, String orderType, String status) {
        Long courseId = coursepublish.getId();
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(XcChooseCourse::getCourseId, courseId).eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getOrderType, orderType).eq(XcChooseCourse::getStatus, status);
        List<XcChooseCourse> chooseCourses = chooseCourseMapper.selectList(queryWrapper);
        if (!chooseCourses.isEmpty()) {
            //已有选课直接返回
            return chooseCourses.get(0);
        }
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType(orderType);
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setStatus(status);
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));
        chooseCourseMapper.insert(xcChooseCourse);
        return xcChooseCourse;
    }


}
