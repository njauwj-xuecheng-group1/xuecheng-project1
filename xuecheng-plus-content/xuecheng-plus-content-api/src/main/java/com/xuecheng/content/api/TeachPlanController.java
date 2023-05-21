package com.xuecheng.content.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.dto.AssociationMediaDto;
import com.xuecheng.content.model.dto.TeachPlanTreeDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachPlanService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/4 9:58
 * @explain:
 */
@RestController
public class TeachPlanController {

    @Resource
    private TeachPlanService teachPlanService;

    @ApiOperation(value = "课程计划查询")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachPlanTreeDto> getTeachPlan(@PathVariable Long courseId) {
        return teachPlanService.getTeachPlanTree(courseId);
    }

    @ApiOperation("新增或修改课程计划")
    @PostMapping("/teachplan")
    public void saveTeachPlan(@RequestBody Teachplan teachplan) {
        teachPlanService.saveTeachPlan(teachplan);
    }

    @ApiOperation(value = "删除课程计划")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachPlan(@PathVariable Long id) {
        teachPlanService.deleteTeachPlanById(id);
    }

    @ApiOperation(value = "课程计划上下移动")
    @PostMapping("/teachplan/{move}/{id}")
    public void move(@PathVariable String move, @PathVariable Long id) {
        teachPlanService.moveUpOrDown(move, id);
    }

    @ApiOperation(value = "绑定媒资")
    @PostMapping("/teachplan/association/media")
    public void associationMedia(@RequestBody AssociationMediaDto associationMediaDto) {
        teachPlanService.associationMedia(associationMediaDto);
    }

    @ApiOperation(value = "解绑媒资")
    @DeleteMapping("/teachplan/association/media/{teachPlanId}/{mediaId}")
    public RestResponse unbindingMedia(@PathVariable Long teachPlanId, @PathVariable String mediaId) {
        return teachPlanService.unbindingMedia(teachPlanId, mediaId);
    }

}
