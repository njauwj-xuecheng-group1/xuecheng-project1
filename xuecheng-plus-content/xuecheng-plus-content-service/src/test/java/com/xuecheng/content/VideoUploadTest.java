package com.xuecheng.content;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author: wj
 * @create_time: 2023/5/8 19:38
 * @explain: 视频分块合并测试
 */
class VideoUploadTest {

    /**
     * 视频分块
     */
    @Test
    void fileBlocking() throws IOException {
        File source = new File("F:\\video\\115-越过action直接commit.avi");
        File blockFile = new File("F:\\video\\block");
        if (!blockFile.exists()) {
            blockFile.mkdir();
        }
        //分块大小
        int chunkSize = 1024 * 1024 * 1;
        //分块个数向上取整
        int chunkNum = (int) Math.ceil(source.length() * 1.0 / chunkSize);
        FileInputStream fileInputStream = new FileInputStream(source);
        byte[] bytes = new byte[chunkSize];
        int length = 0;
        int count = 0;
        while (true) {
            length = fileInputStream.read(bytes);
            if (length == -1) {
                break;
            }
            File file = new File("F:\\video\\block\\" + count++);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes, 0, length);
            fileOutputStream.close();
        }
        fileInputStream.close();
    }

    /**
     * 视频合并
     */
    @Test
    void fileMerge() throws IOException {
        File source = new File("F:\\video\\115-越过action直接commit.avi");
        int chunkSize = 1024 * 1024 * 1;
        int chunkNum = (int) Math.ceil(source.length() * 1.0 / chunkSize);
        File file = new File("F:\\video\\merge_video.avi");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        byte[] bytes = new byte[chunkSize];
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        for (int i = 0; i < chunkNum; i++) {
            File file1 = new File("F:\\video\\block\\" + i);
            FileInputStream fileInputStream = new FileInputStream(file1);
            int length = fileInputStream.read(bytes);
            fileOutputStream.write(bytes, 0, length);
            fileInputStream.close();
        }
        fileOutputStream.close();
        FileInputStream sourceFile = new FileInputStream(source);
        FileInputStream mergeFile = new FileInputStream(file);
        Assertions.assertEquals(DigestUtils.md5DigestAsHex(sourceFile), DigestUtils.md5DigestAsHex(mergeFile));

    }


}
