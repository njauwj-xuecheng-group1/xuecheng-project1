package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.TeachPlanTreeDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/4 10:08
 * @explain:
 */
public interface TeachPlanService {
    List<TeachPlanTreeDto> getTeachPlanTree(Long courseId);

    void saveTeachPlan(Teachplan teachplan);

    void deleteTeachPlanById(Long id);

    void moveUpOrDown(String move, Long id);
}
