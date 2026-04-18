package org.example.fanxing.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Component
public class OssUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${accessKeyId}")
    private String accessKeyId;

    @Value("${accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.folder}")
    private String folder;

    public String uploadFile(MultipartFile file, String fileName) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 上传文件
            ossClient.putObject(bucketName, folder + fileName, file.getInputStream());

            // 返回可访问的 URL
            return "https://" + bucketName + "." + endpoint + "/" + folder + fileName;
        } catch (IOException e) {
            throw new RuntimeException("上传头像失败");
        } finally {
            ossClient.shutdown();
        }
    }
}