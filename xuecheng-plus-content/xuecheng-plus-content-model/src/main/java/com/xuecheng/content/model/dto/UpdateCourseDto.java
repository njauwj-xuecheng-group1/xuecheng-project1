package com.xuecheng.content.model.dto;

import com.xuecheng.base.exception.ValidationGroups;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Positive;

/**
 * @author: wj
 * @create_time: 2023/5/3 17:28
 * @explain:
 */
@Data
public class UpdateCourseDto extends AddCourseDto {

    @ApiModelProperty(value = "课程ID")
    @Positive(message = "id必须为正数", groups = ValidationGroups.ValidationUpdate.class)
    private Long id;
}
