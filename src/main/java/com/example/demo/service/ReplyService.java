package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.ReplyDTO;
import com.example.demo.entity.Reply;

import java.util.List;

public interface ReplyService extends IService<Reply> {

    /**
     * 创建回复
     */
    Reply createReply(Long postId, String username, ReplyDTO.CreateRequest request);

    /**
     * 根据帖子ID获取回复列表
     */
    List<ReplyDTO.Response> getRepliesByPostId(Long postId);

    /**
     * 删除回复
     */
    boolean deleteReply(Long replyId, String username);
}