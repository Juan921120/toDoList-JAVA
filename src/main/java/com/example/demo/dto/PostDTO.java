package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class PostDTO {

    /**
     * 创建帖子请求
     */
    @Data
    public static class CreateRequest {
        private String title;
        private String content;
    }

    /**
     * 帖子列表响应
     */
    @Data
    public static class ListResponse {
        private Long id;
        private String title;
        private String author;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastReplyTime;

        private Integer replyCount;

        // 内容预览（截取前100个字符）
        private String contentPreview;
    }

    /**
     * 帖子详情响应
     */
    @Data
    public static class DetailResponse {
        private Long id;
        private String title;
        private String content;
        private String author;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;

        private Integer replyCount;

        // 回复列表
        private List<ReplyDTO.Response> replies;
    }
}