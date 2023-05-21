package com.xuecheng.content.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.CourseIndex;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;

/**
 * @author: wj
 * @create_time: 2023/5/16 16:56
 * @explain:
 */
@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;

    @Resource
    private SearchServiceClient searchServiceClient;

    @Resource
    private CoursePublishMapper coursePublishMapper;

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

    /**
     * 课程信息保存至redis todo
     *
     * @param mqMessage
     * @param courseId
     */
    private void saveCourseCache(MqMessage mqMessage, Long courseId) {
        MqMessageService mqMessageService = getMqMessageService();
        mqMessageService.completedStageTwo(mqMessage.getId());
        log.debug("将课程信息缓存至redis,课程id:{}", courseId);
    }

    /**
     * 添加课程文档
     *
     * @param mqMessage
     * @param courseId
     */
    private void saveCourseIndex(MqMessage mqMessage, Long courseId) {
        Long id = mqMessage.getId();
        MqMessageService mqMessageService = getMqMessageService();
        int stageThree = mqMessageService.getStageThree(id);
        if (stageThree > 0) {
            log.debug("{}课程文档已保存至索引库", courseId);
            return;
        }
        CourseIndex courseIndex = new CourseIndex();
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        BeanUtils.copyProperties(coursePublish, courseIndex);
        Boolean add = searchServiceClient.add(courseIndex);
        if (Boolean.FALSE.equals(add)) {
            log.error("添加课程至索引发生熔断，课程为{}", courseId);
            XueChengPlusException.cast("添加课程至索引发生熔断");
        }
        mqMessageService.completedStageThree(id);
    }

    /**
     * 页面静态化
     *
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
        File file = coursePublishService.generateStaticHtml(courseId);
        coursePublishService.uploadHtmlToMinio(file, courseId);
        mqMessageService.completedStageOne(id);
    }


}
