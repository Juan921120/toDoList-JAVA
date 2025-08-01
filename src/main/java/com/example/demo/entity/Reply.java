package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("reply")
public class Reply {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long postId;  // 关联的帖子ID

    private String content;

    private String author;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    // 回复层级，0表示直接回复帖子，>0表示回复其他回复
    private Integer level = 0;

    // 父回复ID，如果是回复帖子则为null
    private Long parentReplyId;

    // 被回复的用户名
    private String replyToUser;
}