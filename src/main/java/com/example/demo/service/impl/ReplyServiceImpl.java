package com.example.demo.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.ReplyDTO;
import com.example.demo.entity.Reply;
import com.example.demo.mapper.PostMapper;
import com.example.demo.mapper.ReplyMapper;
import com.example.demo.service.ReplyService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReplyServiceImpl extends ServiceImpl<ReplyMapper, Reply> implements ReplyService {

    @Autowired
    private PostMapper postMapper;

    @Override
    @Transactional
    public Reply createReply(Long postId, String username, ReplyDTO.CreateRequest request) {
        Reply reply = new Reply();
        reply.setPostId(postId);
        reply.setContent(request.getContent());
        reply.setAuthor(username);
        reply.setCreateTime(LocalDateTime.now());

        // 处理回复层级
        if (request.getParentReplyId() != null) {
            Reply parentReply = getById(request.getParentReplyId());
            if (parentReply != null) {
                reply.setLevel(parentReply.getLevel() + 1);
                reply.setParentReplyId(request.getParentReplyId());
                reply.setReplyToUser(request.getReplyToUser());
            }
        } else {
            reply.setLevel(0);
        }

        save(reply);

        // 直接使用PostMapper增加帖子回复数量
        postMapper.incrementReplyCount(postId);

        return reply;
    }

    @Override
    public List<ReplyDTO.Response> getRepliesByPostId(Long postId) {
        List<Reply> replies = lambdaQuery()
                .eq(Reply::getPostId, postId)
                .orderByAsc(Reply::getCreateTime)
                .list();

        return replies.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteReply(Long replyId, String username) {
        Reply reply = getById(replyId);
        if (reply == null || !reply.getAuthor().equals(username)) {
            return false;
        }

        boolean removed = removeById(replyId);
        if (removed) {
            // 直接使用PostMapper减少帖子回复数量
            postMapper.decrementReplyCount(reply.getPostId());
        }

        return removed;
    }

    private ReplyDTO.Response convertToResponse(Reply reply) {
        ReplyDTO.Response response = new ReplyDTO.Response();
        BeanUtils.copyProperties(reply, response);
        return response;
    }
}