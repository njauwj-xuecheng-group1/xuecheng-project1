package com.xuecheng.content.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/12 13:11
 * @explain: 课程预览
 */
@Data
public class CoursePreviewDto {

    private CourseBaseInfoDto courseBase;

    private List<TeachPlanTreeDto> teachplans;

}
