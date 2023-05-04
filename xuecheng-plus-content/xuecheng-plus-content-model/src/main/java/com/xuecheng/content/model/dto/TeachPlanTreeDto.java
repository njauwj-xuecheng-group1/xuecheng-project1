package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;

import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/4 10:03
 * @explain:
 */
@Data
public class TeachPlanTreeDto extends Teachplan {
    List<TeachPlanTreeDto> teachPlanTreeNodes;
    TeachplanMedia teachplanMedia;
}
