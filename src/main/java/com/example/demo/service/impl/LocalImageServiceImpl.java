package com.example.demo.service.impl;

import com.example.demo.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class LocalImageServiceImpl implements ImageService {

    // 图片存储根路径（可在application.yml中配置）
    @Value("${app.upload.image-path:/uploads/images/}")
    private String uploadPath;

    // 服务器访问基础URL（可在application.yml中配置）
    @Value("${app.server.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public String uploadImage(MultipartFile file, String username) throws Exception {

        // 创建上传目录（按日期分组）
        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fullPath = uploadPath + dateFolder;

        Path uploadDir = Paths.get(fullPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadDir.resolve(fileName);

        // 保存文件
        try {
            file.transferTo(filePath.toFile());
        } catch (IOException e) {
            throw new Exception("文件保存失败", e);
        }

        // 返回访问URL
        String relativePath = dateFolder + "/" + fileName;
        return baseUrl + "/images/" + relativePath;
    }

    @Override
    public boolean deleteImage(String imageId, String username) throws Exception {
        // 根据imageId找到文件路径并删除
        // 这里简化处理，实际项目中可能需要数据库记录图片信息
        try {
            Path imagePath = Paths.get(uploadPath, imageId);
            return Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            throw new Exception("删除图片失败", e);
        }
    }
}
