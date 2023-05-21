package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.CourseCategory;
import lombok.Data;

import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/2 20:34
 * @explain:
 */
@Data
public class CourseCategoryTreeDto extends CourseCategory {

    List<CourseCategoryTreeDto> childrenTreeNodes;

}
