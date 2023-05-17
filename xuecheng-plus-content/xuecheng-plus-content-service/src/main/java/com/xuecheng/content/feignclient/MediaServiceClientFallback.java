package com.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author: wj
 * @create_time: 2023/5/17 14:26
 * @explain:
 */
public class MediaServiceClientFallback implements MediaServiceClient {
    @Override
    public String upload(MultipartFile filedata, String objectName) throws IOException {
        return null;
    }
}
