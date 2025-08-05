package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.dto.PostDTO;
import com.example.demo.dto.ReplyDTO;
import com.example.demo.entity.Post;
import com.example.demo.entity.Reply;
import com.example.demo.mapper.PostMapper;
import com.example.demo.mapper.ReplyMapper;
import com.example.demo.service.PostService;
import com.example.demo.utils.HtmlUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private ReplyMapper replyMapper;

    @Override
    public Post createPost(String username, PostDTO.CreateRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle().trim());

        // 清理HTML内容，防止XSS攻击
        String cleanContent = HtmlUtils.sanitizeHtml(request.getContent());
        post.setContent(cleanContent);

        post.setAuthor(username);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        post.setReplyCount(0);

        save(post);
        return post;
    }

    @Override
    public IPage<PostDTO.ListResponse> getPostList(int page, int size) {
        Page<Post> postPage = new Page<>(page, size);

        // 按最后回复时间倒序，如果没有回复则按创建时间倒序
        IPage<Post> posts = lambdaQuery()
                .orderByDesc(Post::getLastReplyTime)
                .orderByDesc(Post::getCreateTime)
                .page(postPage);

        // 转换为DTO
        Page<PostDTO.ListResponse> responsePage = new Page<>(page, size);
        responsePage.setTotal(posts.getTotal());
        responsePage.setPages(posts.getPages());

        List<PostDTO.ListResponse> responseList = posts.getRecords().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        responsePage.setRecords(responseList);
        return responsePage;
    }

    @Override
    public IPage<PostDTO.ListResponse> getMyPostList(String username, int page, int size) {
        Page<Post> postPage = new Page<>(page, size);

        // 查询当前用户的帖子，按创建时间倒序
        IPage<Post> posts = lambdaQuery()
                .eq(Post::getAuthor, username)  // 过滤条件：只查询当前用户的帖子
                .orderByDesc(Post::getLastReplyTime)
                .orderByDesc(Post::getCreateTime)
                .page(postPage);

        // 转换为DTO
        Page<PostDTO.ListResponse> responsePage = new Page<>(page, size);
        responsePage.setTotal(posts.getTotal());
        responsePage.setPages(posts.getPages());

        List<PostDTO.ListResponse> responseList = posts.getRecords().stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        responsePage.setRecords(responseList);
        return responsePage;
    }

    @Override
    public PostDTO.DetailResponse getPostDetail(Long postId) {
        Post post = getById(postId);
        if (post == null) {
            return null;
        }

        PostDTO.DetailResponse response = new PostDTO.DetailResponse();
        BeanUtils.copyProperties(post, response);

        // 直接获取回复列表，避免循环依赖
        LambdaQueryWrapper<Reply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reply::getPostId, postId)
                .orderByAsc(Reply::getCreateTime);
        List<Reply> replies = replyMapper.selectList(wrapper);

        List<ReplyDTO.Response> replyResponses = replies.stream()
                .map(this::convertToReplyResponse)
                .collect(Collectors.toList());

        response.setReplies(replyResponses);

        return response;
    }

    private PostDTO.ListResponse convertToListResponse(Post post) {
        PostDTO.ListResponse response = new PostDTO.ListResponse();
        BeanUtils.copyProperties(post, response);

        // 使用HtmlUtils生成富文本内容的预览
        response.setContentPreview(HtmlUtils.generatePreview(post.getContent(), 100));

        return response;
    }

    private ReplyDTO.Response convertToReplyResponse(Reply reply) {
        ReplyDTO.Response response = new ReplyDTO.Response();
        BeanUtils.copyProperties(reply, response);
        return response;
    }

    @Override
    @Transactional
    public boolean deletePost(Long postId, String username) {
        // 1. 检查帖子是否存在
        Post post = getById(postId);
        if (post == null) {
            return false;  // 帖子不存在
        }

        // 2. 检查权限：只有作者可以删除
        if (!post.getAuthor().equals(username)) {
            return false;  // 权限不足
        }

        // 3. 执行删除（回复会自动级联删除）
        return removeById(postId);
    }
}