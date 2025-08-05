package com.example.demo.web;

import com.example.demo.dto.ErrorResponse;
import com.example.demo.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    // 允许的图片类型
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    // 最大文件大小 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 上传图片接口 - 供富文本编辑器使用
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        // 验证用户登录
        String username = (String) request.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401)
                    .body(ErrorResponse.of("UNAUTHORIZED", "请先登录"));
        }

        // 验证文件是否为空
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("EMPTY_FILE", "请选择要上传的图片"));
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("FILE_TOO_LARGE", "图片大小不能超过5MB"));
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_FILE_TYPE", "只支持 JPG、PNG、GIF、WebP 格式的图片"));
        }

        try {
            // 上传图片并获取URL
            String imageUrl = imageService.uploadImage(file, username);

            // 返回图片URL给前端
            Map<String, Object> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("filename", file.getOriginalFilename());
            response.put("size", file.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("UPLOAD_FAILED", "图片上传失败：" + e.getMessage()));
        }
    }

    /**
     * 删除图片接口（可选）
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteImage(
            @PathVariable String imageId,
            HttpServletRequest request) {

        String username = (String) request.getAttribute("username");

        try {
            boolean deleted = imageService.deleteImage(imageId, username);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(403)
                        .body(ErrorResponse.of("ACCESS_DENIED", "没有权限删除此图片或图片不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("DELETE_FAILED", "删除图片失败：" + e.getMessage()));
        }
    }
}