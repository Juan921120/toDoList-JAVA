package com.example.demo.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PostDTO;
import com.example.demo.dto.ReplyDTO;
import com.example.demo.entity.Post;
import com.example.demo.entity.Reply;
import com.example.demo.service.PostService;
import com.example.demo.service.ReplyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private ReplyService replyService;

    /**
     * 创建帖子
     */
    @PostMapping
    public ApiResponse<Post> createPost(
            @RequestBody PostDTO.CreateRequest request,
            HttpServletRequest httpRequest) {

        // 参数验证
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ApiResponse.fail("标题不能为空");
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ApiResponse.fail("内容不能为空");
        }

        // 从JWT过滤器中获取用户名
        String username = (String) httpRequest.getAttribute("username");

        try {
            Post post = postService.createPost(username, request);
            return ApiResponse.ok("帖子创建成功", post);
        } catch (Exception e) {
            return ApiResponse.fail("创建帖子失败：" + e.getMessage());
        }
    }

    /**
     * 获取帖子列表（分页）
     */
    @GetMapping
    public ApiResponse<IPage<PostDTO.ListResponse>> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10; // 限制每页最多50条

        try {
            IPage<PostDTO.ListResponse> posts = postService.getPostList(page, size);
            return ApiResponse.ok("获取帖子列表成功", posts);
        } catch (Exception e) {
            return ApiResponse.fail("获取帖子列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{postId}")
    public ApiResponse<PostDTO.DetailResponse> getPostDetail(@PathVariable Long postId) {

        try {
            PostDTO.DetailResponse post = postService.getPostDetail(postId);
            if (post == null) {
                return ApiResponse.fail("帖子不存在");
            }

            return ApiResponse.ok("获取帖子详情成功", post);
        } catch (Exception e) {
            return ApiResponse.fail("获取帖子详情失败：" + e.getMessage());
        }
    }

    /**
     * 回复帖子
     */
    @PostMapping("/{postId}/replies")
    public ApiResponse<Reply> replyToPost(
            @PathVariable Long postId,
            @RequestBody ReplyDTO.CreateRequest request,
            HttpServletRequest httpRequest) {

        // 参数验证
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ApiResponse.fail("回复内容不能为空");
        }

        // 检查帖子是否存在
        PostDTO.DetailResponse post = postService.getPostDetail(postId);
        if (post == null) {
            return ApiResponse.fail("帖子不存在");
        }

        // 从JWT过滤器中获取用户名
        String username = (String) httpRequest.getAttribute("username");

        try {
            Reply reply = replyService.createReply(postId, username, request);
            return ApiResponse.ok("回复成功", reply);
        } catch (Exception e) {
            return ApiResponse.fail("回复失败：" + e.getMessage());
        }
    }

    /**
     * 获取帖子的回复列表
     */
    @GetMapping("/{postId}/replies")
    public ApiResponse<java.util.List<ReplyDTO.Response>> getReplies(@PathVariable Long postId) {

        // 检查帖子是否存在
        PostDTO.DetailResponse post = postService.getPostDetail(postId);
        if (post == null) {
            return ApiResponse.fail("帖子不存在");
        }

        try {
            java.util.List<ReplyDTO.Response> replies = replyService.getRepliesByPostId(postId);
            return ApiResponse.ok("获取回复列表成功", replies);
        } catch (Exception e) {
            return ApiResponse.fail("获取回复列表失败：" + e.getMessage());
        }
    }

    /**
     * 删除回复
     */
    @DeleteMapping("/replies/{replyId}")
    public ApiResponse<Void> deleteReply(
            @PathVariable Long replyId,
            HttpServletRequest httpRequest) {

        // 从JWT过滤器中获取用户名
        String username = (String) httpRequest.getAttribute("username");

        try {
            boolean deleted = replyService.deleteReply(replyId, username);
            if (deleted) {
                return ApiResponse.ok("删除回复成功");
            } else {
                return ApiResponse.fail("没有权限删除此回复或回复不存在");
            }
        } catch (Exception e) {
            return ApiResponse.fail("删除回复失败：" + e.getMessage());
        }
    }
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@PathVariable Long postId, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");

        boolean deleted = postService.deletePost(postId, username);
        if (deleted) {
            return ApiResponse.ok("删除帖子成功");
        } else {
            return ApiResponse.fail("没有权限删除此帖子或帖子不存在");
        }
    }



}