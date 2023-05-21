package com.xuecheng.content.api;

import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.UpdateCourseDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.utils.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author wj
 * @version 1.0
 * @description 课程信息编辑接口
 * @date 2022/9/6 11:29
 */
@RestController
@Api(value = "课程信息管理", tags = "课程信息管理接口")
public class CourseBaseInfoController {

    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation(value = "课程信息查询")
    @PreAuthorize("hasAnyAuthority('xc_teachmanager_course')")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {
        return courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParams);
    }

    @ApiOperation(value = "新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto addCourse(@RequestBody @Validated(value = ValidationGroups.ValidationInsert.class) AddCourseDto addCourseDto) {
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId, addCourseDto);
    }

    @ApiOperation(value = "查询课程信息")
    @GetMapping("/course/{id}")
    public CourseBaseInfoDto getCourse(@PathVariable Long id) {
        if (id <= 0) {
            XueChengPlusException.cast(CommonError.PARAMS_ERROR);
        }
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        return courseBaseInfoService.getCourseById(id);
    }

    @ApiOperation(value = "修改课程信息")
    @PutMapping("/course")
    public CourseBaseInfoDto updateCourse(@RequestBody @Validated(value = ValidationGroups.ValidationUpdate.class) UpdateCourseDto updateCourseDto) {
        //机构id，由于认证系统没有上线暂时硬编码
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId, updateCourseDto);
    }

    @ApiOperation(value = "删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable Long courseId) {
        courseBaseInfoService.deleteCourse(courseId);
    }

}
