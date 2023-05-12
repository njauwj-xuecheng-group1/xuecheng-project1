package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseMarketService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author: wj
 * @create_time: 2023/5/3 8:00
 * @explain:
 */
@Service
public class CourseMarketServiceImpl implements CourseMarketService {

    @Resource
    private CourseMarketMapper courseMarketMapper;

    @Override
    public CourseMarket getCourseMarketById(Long courseId) {
        return courseMarketMapper.selectById(courseId);
    }
}
