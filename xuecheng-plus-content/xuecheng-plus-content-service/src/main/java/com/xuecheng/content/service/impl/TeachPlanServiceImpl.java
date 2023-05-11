package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.TeachPlanTreeDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/4 10:08
 * @explain:
 */
@Service
public class TeachPlanServiceImpl implements TeachPlanService {

    @Resource
    private TeachplanMapper teachplanMapper;

    @Resource
    private TeachplanMediaMapper teachplanMediaMapper;


    /**
     * 查询课程计划
     *
     * @param courseId
     * @return
     */
    @Override
    public List<TeachPlanTreeDto> getTeachPlanTree(Long courseId) {
        return teachplanMapper.getTeanPlanByCourseId(courseId);
    }

    /**
     * 新增章节或修改章节
     *
     * @param teachplan
     */
    @Override
    public void saveTeachPlan(Teachplan teachplan) {
        if (teachplan.getId() == null) {
            //新增操作
            Integer orderBy;
            if (teachplan.getGrade() == 1) {
                //新增章节
                orderBy = teachplanMapper.getMaxOrderBy(teachplan.getCourseId(), 0L);

            } else {
                //新增小节
                orderBy = teachplanMapper.getMaxOrderBy(teachplan.getCourseId(), teachplan.getParentid());
            }
            if (orderBy == null) {
                orderBy = 0;
            }
            teachplan.setOrderby(orderBy + 1);
            teachplanMapper.insert(teachplan);
            return;
        }
        //更新操作
        teachplanMapper.updateById(teachplan);
    }

    /**
     * 删除课程计划
     *
     * @param id 课程计划ID
     */
    @Override
    @Transactional
    public void deleteTeachPlanById(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan.getGrade() == 2) {
            //小章节直接删除即可
            teachplanMapper.deleteById(id);
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachplanMedia::getTeachplanId, id);
            teachplanMediaMapper.delete(queryWrapper);
        } else {
            //大章节如果没有小章节即可删除
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid, id);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            if (count > 0) {
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
            }
            teachplanMapper.deleteById(id);
        }
    }

    /**
     * @param move moveup向上移动 movedown向下移动
     * @param id   课程计划Id
     */
    @Override
    @Transactional
    public void moveUpOrDown(String move, Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, teachplan.getCourseId());
        if (move.equals("moveup")) {
            //orderby 越小越靠前
            queryWrapper.lt(Teachplan::getOrderby, teachplan.getOrderby());
            queryWrapper.orderByDesc(Teachplan::getOrderby);
        } else {
            queryWrapper.gt(Teachplan::getOrderby, teachplan.getOrderby());
            queryWrapper.orderByAsc(Teachplan::getOrderby);
        }
        queryWrapper.last("limit 1");
        if (teachplan.getGrade() == 1) {
            //大章节移动
            queryWrapper.eq(Teachplan::getGrade, 1);
        } else {
            //小章节移动
            queryWrapper.eq(Teachplan::getParentid, teachplan.getParentid());
        }
        Teachplan exchangeObj = teachplanMapper.selectOne(queryWrapper);
        if (exchangeObj == null) {
            return;
        }
        Integer orderby1 = teachplan.getOrderby();
        Integer orderby2 = exchangeObj.getOrderby();
        teachplan.setOrderby(orderby2);
        exchangeObj.setOrderby(orderby1);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(exchangeObj);
    }
}
