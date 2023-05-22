package com.xuecheng.learning.service;

import com.xuecheng.learning.model.dto.XcChooseCourseDto;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;

/**
 * @author: wj
 * @create_time: 2023/5/22 15:03
 * @explain:
 */
public interface ChooseCourseService {



    XcChooseCourseDto addChooseCourse(Long courseId, String userId);

    XcCourseTablesDto getLearnstatus(Long courseId, String userId);

}
