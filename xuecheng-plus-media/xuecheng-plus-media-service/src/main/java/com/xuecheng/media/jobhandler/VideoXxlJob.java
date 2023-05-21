package com.xuecheng.media.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 视频转码任务
 */
@Component
@Slf4j
public class VideoXxlJob {

    @Resource
    private MediaProcessService mediaProcessService;

    @Resource
    private MediaFileService mediaFileService;

    //ffmpeg的安装位置
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpeg_path;

    //ffmpeg的安装位置
    @Value("${minio.bucket.videofiles}")
    private String fileType;

    /**
     * 2、分片广播任务
     */
    @XxlJob("videoJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); //执行器序号
        int shardTotal = XxlJobHelper.getShardTotal(); //执行器个数
        //1. 得到cpu的核心数
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        //2. 获取任务
        List<MediaProcess> tasks = mediaProcessService.getTasksByShardIndex(shardIndex, shardTotal, availableProcessors);
        if (tasks.isEmpty()) {
            log.debug("当前没有任务");
            return;
        }
        //3. 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(tasks.size());
        CountDownLatch countDownLatch = new CountDownLatch(tasks.size());
        tasks.forEach(task -> executorService.execute(() -> {
            try {
                //判断任务是否正在执行中
                boolean startTask = mediaProcessService.startTask(task.getId());
                if (!startTask) {
                    log.error("任务正在被执行中，抢占失败,任务ID{}", task.getId());
                    return;
                }
                proccessTask(task);
            } finally {
                countDownLatch.countDown();
            }
        }));
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    //开始执行任务
    private void proccessTask(MediaProcess task) {
        //从minio下载视频
        File file = mediaFileService.downloadFileFromMinIO(task.getBucket(), task.getFilePath());
        if (file == null) {
            log.error("从minio下载视频失败,任务ID{},bucket{},filePath{}", task.getId(), task.getBucket(), task.getFilePath());
            mediaProcessService.saveProcessFinishStatus(task.getId(), "3", task.getFileId(), null, "从minio下载视频失败");
            return;
        }
        //源avi视频的路径
        String video_path = file.getAbsolutePath();
        //转换后mp4文件的名称
        String mp4_name = task.getFilename() + ".mp4";
        //转换后mp4文件的路径
        File tempFile = null;
        try {
            tempFile = File.createTempFile("minio", ".mp4");
        } catch (IOException e) {
            log.error("创建临时文件失败，任务ID{},bucket{},filePath{}", task.getId(), task.getBucket(), task.getFilePath());
            mediaProcessService.saveProcessFinishStatus(task.getId(), "3", task.getFileId(), null, "创建临时文件失败");
            return;
        }
        String mp4_path = tempFile.getAbsolutePath();
        //创建工具类对象
        Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4_path);
        //开始视频转换，成功将返回success
        String s = videoUtil.generateMp4();
        if (!s.equals("success")) {
            log.error("视频转换失败，任务ID{},bucket{},filePath{}", task.getId(), task.getBucket(), task.getFilePath());
            mediaProcessService.saveProcessFinishStatus(task.getId(), "3", task.getFileId(), null, "视频转换失败");
            return;
        }
        String url = "/" + fileType + "/" + mediaFileService.getFilePathByMd5(task.getFileId(), ".mp4");
        //将视频上传到minio
        boolean b = mediaFileService.addMediaFilesToMinIO(mp4_path, mediaFileService.getMimeType(".mp4"), task.getBucket(), mediaFileService.getFilePathByMd5(task.getFileId(), ".mp4"));
        if (!b) {
            log.error("视频转码后上传到minio失败，任务ID{},bucket{},filePath{}", task.getId(), task.getBucket(), task.getFilePath());
            mediaProcessService.saveProcessFinishStatus(task.getId(), "3", task.getFileId(), null, "视频转码后上传到minio失败");
            return;
        }

        mediaProcessService.saveProcessFinishStatus(task.getId(), "2", task.getFileId(), url, null);

    }

}
