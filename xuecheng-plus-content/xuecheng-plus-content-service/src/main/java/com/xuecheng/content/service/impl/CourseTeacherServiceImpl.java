package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/4 16:42
 * @explain:
 */
@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Resource
    private CourseTeacherMapper courseTeacherMapper;

    /**
     * 查询教师
     *
     * @param courseId
     * @return
     */
    @Override
    public List<CourseTeacher> getCourseTeachers(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    /**
     * 添加或更新教师
     *
     * @param courseTeacher
     * @return
     */
    @Override
    public CourseTeacher addCourseTeacher(CourseTeacher courseTeacher) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseTeacher.getCourseId());
        queryWrapper.eq(CourseTeacher::getTeacherName, courseTeacher.getTeacherName());
        Integer count = courseTeacherMapper.selectCount(queryWrapper);
        if (courseTeacher.getId() == null) {
            //新增老师
            if (count > 0) {
                XueChengPlusException.cast("已存在教师");
            }
            courseTeacherMapper.insert(courseTeacher);
        } else {
            //更新老师信息
            if (count > 1) {
                XueChengPlusException.cast("已存在教师");
            }
            courseTeacherMapper.updateById(courseTeacher);
        }
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }


    /**
     * 删除教师
     *
     * @param courseId
     * @param teacherId
     */
    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId);
        queryWrapper.eq(CourseTeacher::getId, teacherId);
        courseTeacherMapper.delete(queryWrapper);
    }

}
