package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/4 16:42
 * @explain:
 */
public interface CourseTeacherService {
    List<CourseTeacher> getCourseTeachers(Long courseId);

    CourseTeacher addCourseTeacher(CourseTeacher courseTeacher);


    void deleteCourseTeacher(Long courseId, Long teacherId);
}
