package com.example.demo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.dto.PostDTO;
import com.example.demo.entity.Post;
import org.springframework.transaction.annotation.Transactional;

public interface PostService extends IService<Post> {

    /**
     * 创建帖子
     */
    Post createPost(String username, PostDTO.CreateRequest request);

    /**
     * 获取所有帖子列表（分页）
     */
    IPage<PostDTO.ListResponse> getPostList(int page, int size);

    /**
     * 新增：获取当前用户的帖子列表（分页）
     */
    IPage<PostDTO.ListResponse> getMyPostList(String username, int page, int size);


    /**
     * 获取帖子详情（包含回复）
     */
    PostDTO.DetailResponse getPostDetail(Long postId);

    /**删除自己帖子
     */
    public boolean deletePost(Long postId, String username);

}