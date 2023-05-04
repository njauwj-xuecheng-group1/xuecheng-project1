package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/4 16:39
 * @explain:
 */
@RestController
@Api(value = "课程教师管理")
public class CourseTeacherController {

    @Resource
    private CourseTeacherService courseTeacherService;

    @ApiOperation(value = "获取课程老师列表")
    @GetMapping("/courseTeacher/list/{courseId}")
    public List<CourseTeacher> getCourseTeachers(@PathVariable Long courseId) {
        return courseTeacherService.getCourseTeachers(courseId);
    }

    @ApiOperation(value = "添加课程老师/更新课程老师信息")
    @PostMapping("/courseTeacher")
    public CourseTeacher addCourseTeacher(@RequestBody CourseTeacher courseTeacher) {
        return courseTeacherService.addCourseTeacher(courseTeacher);
    }


    @ApiOperation(value = "删除课程老师")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void updateCourseTeacher(@PathVariable Long courseId, @PathVariable Long teacherId) {
        courseTeacherService.deleteCourseTeacher(courseId, teacherId);
    }

}
