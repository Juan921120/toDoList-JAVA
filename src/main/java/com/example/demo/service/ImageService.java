package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    /**
     * 上传图片
     * @param file 图片文件
     * @param username 用户名
     * @return 图片访问URL
     */
    String uploadImage(MultipartFile file, String username) throws Exception;

    /**
     * 删除图片
     * @param imageId 图片ID
     * @param username 用户名
     * @return 是否删除成功
     */
    boolean deleteImage(String imageId, String username) throws Exception;
}