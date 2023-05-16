package com.xuecheng.content.jobhandler;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: wj
 * @create_time: 2023/5/16 16:56
 * @explain:
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {


    @XxlJob("CoursePublishJobHandler")
    public void shardingJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); //执行器序号
        int shardTotal = XxlJobHelper.getShardTotal(); //执行器个数
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }


    @Override
    @Transactional
    public boolean execute(MqMessage mqMessage) {
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        //课程静态化
        generateHtml(mqMessage, courseId);
        //课程索引
        saveCourseIndex(mqMessage, courseId);
        //课程缓存
        saveCourseCache(mqMessage, courseId);

        return true;
    }

    private void saveCourseCache(MqMessage mqMessage, Long courseId) {
        log.debug("将课程信息缓存至redis,课程id:{}", courseId);
    }

    private void saveCourseIndex(MqMessage mqMessage, Long courseId) {
        log.debug("保存课程索引信息,课程id:{}", courseId);
    }

    /**
     * 页面静态化
     * @param mqMessage
     * @param courseId
     */
    private void generateHtml(MqMessage mqMessage, Long courseId) {
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = getMqMessageService();
        int stageOne = mqMessageService.getStageOne(id);
        if (stageOne > 0) {
            log.debug("{}课程静态页面已生成", courseId);
            return;
        }
        int i = 1 / 0;
        mqMessageService.completedStageOne(id);
    }


}
