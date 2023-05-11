package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/10 15:56
 * @explain:
 */
public interface MediaProcessService {

    List<MediaProcess> getTasksByShardIndex(Integer shardIndex, Integer shardTotal, Integer count);

    /**
     * 基于数据库实现乐观锁
     * @return
     * @param id
     */
    boolean startTask(Long id);

    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}
