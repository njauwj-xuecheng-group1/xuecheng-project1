package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;

/**
 * @author: wj
 * @create_time: 2023/5/12 11:43
 * @explain:
 */
@Controller
@Api("课程发布")
public class CoursePublishController {

    @Resource
    private CoursePublishService coursePublishService;

    @ApiOperation("课程预览")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {
        ModelAndView modelAndView = new ModelAndView();
        CoursePreviewDto coursePreviewDto = coursePublishService.preview(courseId);
        modelAndView.addObject("model", coursePreviewDto);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    @ApiOperation("课程审核")
    @PostMapping("/courseaudit/commit/{courseId}")
    public void courseAudit(@PathVariable Long courseId) {
        Long companyId = 1232141425L;
        coursePublishService.courseAudit(companyId, courseId);
    }

}
