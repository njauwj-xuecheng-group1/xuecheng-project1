package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: wj
 * @create_time: 2023/5/2 19:18
 * @explain:
 */
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    public List<CourseCategoryTreeDto> getCourseCategory(String id) {
        List<CourseCategoryTreeDto> courseCategoryTree = courseCategoryMapper.getCourseCategoryTree(id);
        List<CourseCategoryTreeDto> parent = new ArrayList<>();
        Map<String, CourseCategoryTreeDto> treeMap = new HashMap<>();
        courseCategoryTree.forEach(courseCategoryTreeDto -> {
            if (id.equals(courseCategoryTreeDto.getParentid())) {
                parent.add(courseCategoryTreeDto);
            }
            if (courseCategoryTreeDto.getIsLeaf().equals(0)) {
                //说明不是叶子结点
                courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<>());
                treeMap.put(courseCategoryTreeDto.getId(), courseCategoryTreeDto);
            }
        });
        courseCategoryTree.forEach(courseCategoryTreeDto -> {
            if (treeMap.containsKey(courseCategoryTreeDto.getParentid())) {
                CourseCategoryTreeDto father = treeMap.get(courseCategoryTreeDto.getParentid());
                father.getChildrenTreeNodes().add(courseCategoryTreeDto);
            }
        });
        return parent;
    }

}
