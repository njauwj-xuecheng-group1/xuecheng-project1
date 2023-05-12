package com.xuecheng.content.service.impl;

import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachPlanTreeDto;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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


    @Override
    public CoursePreviewDto preview(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.getCourseById(courseId);
        List<TeachPlanTreeDto> teachPlans = teachPlanService.getTeachPlanTree(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachPlans);

        return coursePreviewDto;
    }
}
