package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/2 21:38
 * @explain:
 */
@RestController
@Api(value = "课程分类管理")
public class CourseCategoryController {

    @Resource
    private CourseCategoryService courseCategoryService;

    @ApiOperation(value = "课程分类查询")
    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryCourseCategoryTree() {
        return courseCategoryService.getCourseCategory("1");
    }
}
