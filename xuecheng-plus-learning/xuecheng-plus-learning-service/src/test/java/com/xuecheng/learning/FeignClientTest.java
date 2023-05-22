package com.xuecheng.learning;

import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Mr.M
 * @version 1.0
 * @description
 * @date 2023/2/22 20:14
 */
@SpringBootTest
class FeignClientTest {

    @Autowired
    ContentServiceClient contentServiceClient;


    @Test
    void testContentServiceClient() {
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(18L);
        Assertions.assertNotNull(coursepublish);
    }
}