package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/**
 * @author: wj
 * @create_time: 2023/5/8 20:57
 * @explain: 实现视频的分块与合并上传
 */
@Api(value = "大文件上传接口", tags = "大文件上传接口")
@RestController
public class BigFileController {

    @Resource
    private MediaFileService mediaFileService;


    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkFile(@RequestParam("fileMd5") String fileMd5) {
        return mediaFileService.checkFile(fileMd5);
    }


    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5, @RequestParam("chunk") int chunk) {

        return mediaFileService.checkChunk(fileMd5, chunk);
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadChunk(@RequestParam("file") MultipartFile file, @RequestParam("fileMd5") String fileMd5, @RequestParam("chunk") int chunk) {
        //创建一个临时文件
        File tempFile = null;
        try {
            tempFile = File.createTempFile("minio", ".temp");
            file.transferTo(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return RestResponse.validfail("系统错误");
        }
        //文件路径
        String localChunkFilePath = tempFile.getAbsolutePath();
        return mediaFileService.uploadChunk(fileMd5, chunk, localChunkFilePath);
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergeChunks(@RequestParam("fileMd5") String fileMd5, @RequestParam("fileName") String fileName, @RequestParam("chunkTotal") int chunkTotal) {
        Long companyId = 1232141425L;
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setFileType("001002");
        uploadFileParamsDto.setTags("视频文件");
        return mediaFileService.mergeChunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);
    }
}
