package com.example.demo.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.dto.ErrorResponse;  // 新增导入
import com.example.demo.dto.PostDTO;
import com.example.demo.dto.ReplyDTO;
import com.example.demo.entity.Post;
import com.example.demo.entity.Reply;
import com.example.demo.service.PostService;
import com.example.demo.service.ReplyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;  // 新增导入
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
    public ResponseEntity<?> createPost(  // 修改返回类型
                                          @RequestBody PostDTO.CreateRequest request,
                                          HttpServletRequest httpRequest) {

        // 参数验证
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest()  // 修改返回方式
                    .body(ErrorResponse.of("INVALID_TITLE", "标题不能为空"));
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest()  // 修改返回方式
                    .body(ErrorResponse.of("INVALID_CONTENT", "内容不能为空"));
        }

        // 从JWT过滤器中获取用户名
        String username = (String) httpRequest.getAttribute("username");

        try {
            Post post = postService.createPost(username, request);
            return ResponseEntity.ok(post);  // 修改返回方式
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("CREATE_POST_FAILED", "创建帖子失败：" + e.getMessage()));
        }
    }

    /**
     * 获取帖子列表（分页）
     */
    @GetMapping
    public ResponseEntity<?> getPostList(  // 修改返回类型
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {

        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10; // 限制每页最多50条

        try {
            IPage<PostDTO.ListResponse> posts = postService.getPostList(page, size);
            return ResponseEntity.ok(posts);  // 修改返回方式
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("GET_POSTS_FAILED", "获取帖子列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetail(@PathVariable Long postId) {  // 修改返回类型

        try {
            PostDTO.DetailResponse post = postService.getPostDetail(postId);
            if (post == null) {
                return ResponseEntity.notFound().build();  // 修改返回方式 - 直接404
            }

            return ResponseEntity.ok(post);  // 修改返回方式
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("GET_POST_FAILED", "获取帖子详情失败：" + e.getMessage()));
        }
    }

    /**
     * 回复帖子
     */
    @PostMapping("/{postId}/replies")
    public ResponseEntity<?> replyToPost(  // 修改返回类型
                                           @PathVariable Long postId,
                                           @RequestBody ReplyDTO.CreateRequest request,
                                           HttpServletRequest httpRequest) {

        // 参数验证
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest()  // 修改返回方式
                    .body(ErrorResponse.of("INVALID_CONTENT", "回复内容不能为空"));
        }

        // 检查帖子是否存在
        PostDTO.DetailResponse post = postService.getPostDetail(postId);
        if (post == null) {
            return ResponseEntity.notFound().build();  // 修改返回方式 - 直接404
        }

        // 从JWT过滤器中获取用户名
        String username = (String) httpRequest.getAttribute("username");

        try {
            Reply reply = replyService.createReply(postId, username, request);
            return ResponseEntity.ok(reply);  // 修改返回方式
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("REPLY_FAILED", "回复失败：" + e.getMessage()));
        }
    }

    /**
     * 获取帖子的回复列表
     */
    @GetMapping("/{postId}/replies")
    public ResponseEntity<?> getReplies(@PathVariable Long postId) {  // 修改返回类型

        // 检查帖子是否存在
        PostDTO.DetailResponse post = postService.getPostDetail(postId);
        if (post == null) {
            return ResponseEntity.notFound().build();  // 修改返回方式 - 直接404
        }

        try {
            java.util.List<ReplyDTO.Response> replies = replyService.getRepliesByPostId(postId);
            return ResponseEntity.ok(replies);  // 修改返回方式
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("GET_REPLIES_FAILED", "获取回复列表失败：" + e.getMessage()));
        }
    }

    /**
     * 删除回复
     */
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<?> deleteReply(  // 修改返回类型
                                           @PathVariable Long replyId,
                                           HttpServletRequest httpRequest) {

        // 从JWT过滤器中获取用户名
        String username = (String) httpRequest.getAttribute("username");

        try {
            boolean deleted = replyService.deleteReply(replyId, username);
            if (deleted) {
                return ResponseEntity.noContent().build();  // 修改返回方式 - 204 No Content
            } else {
                return ResponseEntity.status(403)  // 修改返回方式 - 403 Forbidden
                        .body(ErrorResponse.of("ACCESS_DENIED", "没有权限删除此回复或回复不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("DELETE_REPLY_FAILED", "删除回复失败：" + e.getMessage()));
        }
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, HttpServletRequest request) {  // 修改返回类型
        String username = (String) request.getAttribute("username");

        try {
            boolean deleted = postService.deletePost(postId, username);
            if (deleted) {
                return ResponseEntity.noContent().build();  // 修改返回方式 - 204 No Content
            } else {
                return ResponseEntity.status(403)  // 修改返回方式 - 403 Forbidden
                        .body(ErrorResponse.of("ACCESS_DENIED", "没有权限删除此帖子或帖子不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("DELETE_POST_FAILED", "删除帖子失败：" + e.getMessage()));
        }
    }
}