package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: wj
 * @create_time: 2023/5/10 15:57
 * @explain:
 */
@Slf4j
@Service
public class MediaProcessServiceImpl implements MediaProcessService {

    @Resource
    private MediaProcessMapper mediaProcessMapper;

    @Resource
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Resource
    private MediaFilesMapper mediaFilesMapper;

    @Override
    public List<MediaProcess> getTasksByShardIndex(Integer shardIndex, Integer shardTotal, Integer count) {
        return mediaProcessMapper.selectTasksByShardIndex(shardIndex, shardTotal, count);
    }

    @Override
    public boolean startTask(Long id) {
        return mediaProcessMapper.startTask(id) > 0;
    }

    /**
     * 任务处理完成后更新数据库
     *
     * @param taskId
     * @param status
     * @param fileId
     * @param url
     * @param errorMsg
     */
    @Override
    @Transactional
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            return;
        }
        if (status.equals("3")) {
            //任务失败
            mediaProcess.setStatus("3");
            mediaProcess.setErrormsg(errorMsg);
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcessMapper.updateById(mediaProcess);
            log.error("任务执行失败，taskId{}，errorMsg{}", taskId, errorMsg);
            return;
        }
        mediaProcess.setStatus("2");
        mediaProcess.setUrl(url);
        mediaProcess.setFinishDate(LocalDateTime.now());
        //任务执行成功保存到history
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        //更新mediaFile的url
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);
        //删除任务
        mediaProcessMapper.deleteById(mediaProcess);
    }
}
