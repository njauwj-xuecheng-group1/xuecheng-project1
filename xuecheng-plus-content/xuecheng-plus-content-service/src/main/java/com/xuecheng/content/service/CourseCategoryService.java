package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/2 19:17
 * @explain: 课程类别
 */
public interface CourseCategoryService {
    List<CourseCategoryTreeDto> getCourseCategory(String id);
}
