package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

public class ReplyDTO {

    /**
     * 创建回复请求
     */
    @Data
    public static class CreateRequest {
        private String content;
        private Long parentReplyId;  // 可选，回复特定回复时使用
        private String replyToUser;  // 可选，被回复的用户名
    }

    /**
     * 回复响应
     */
    @Data
    public static class Response {
        private Long id;
        private String content;
        private String author;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;

        private Integer level;
        private Long parentReplyId;
        private String replyToUser;
    }
}