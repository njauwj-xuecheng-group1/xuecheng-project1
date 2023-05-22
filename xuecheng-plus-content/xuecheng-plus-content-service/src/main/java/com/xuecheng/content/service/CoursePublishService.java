package com.xuecheng.content.service;


import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

/**
 * @author: wj
 * @create_time: 2023/5/12 13:17
 * @explain:
 */
public interface CoursePublishService {

    CoursePreviewDto preview(Long courseId);

    void courseAudit(Long companyId, Long courseId);

    void publish(Long companyId, Long courseId);

    File generateStaticHtml(Long courseId);

    void uploadHtmlToMinio(File file, Long courseId);

    CoursePublish getCoursepublish(Long courseId);
}
