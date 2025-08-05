package com.example.demo.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.dto.ErrorResponse;
import com.example.demo.dto.PostDTO;
import com.example.demo.dto.ReplyDTO;
import com.example.demo.entity.Post;
import com.example.demo.entity.Reply;
import com.example.demo.service.PostService;
import com.example.demo.service.ReplyService;
import com.example.demo.utils.HtmlUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.PageResponse;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private ReplyService replyService;

    // 帖子内容最大长度（包含HTML标签）
    private static final int MAX_POST_CONTENT_LENGTH = 200000; // 增加到20万字符，适应富文本HTML
    private static final int MAX_POST_TEXT_LENGTH = 10000;     // 纯文本内容限制
    private static final int MAX_REPLY_CONTENT_LENGTH = 50000; // 回复富文本限制
    private static final int MAX_REPLY_TEXT_LENGTH = 2000;     // 回复纯文本限制
    private static final int MAX_TITLE_LENGTH = 200;

    /**
     * 创建帖子
     */
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostDTO.CreateRequest request,
                                        HttpServletRequest httpRequest) {

        // 参数验证
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_TITLE", "标题不能为空"));
        }

        if (request.getTitle().trim().length() > MAX_TITLE_LENGTH) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("TITLE_TOO_LONG", "标题长度不能超过" + MAX_TITLE_LENGTH + "个字符"));
        }

        if (request.getContent() == null || HtmlUtils.isContentEmpty(request.getContent())) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_CONTENT", "内容不能为空"));
        }

        if (HtmlUtils.isContentTooLong(request.getContent(), MAX_POST_CONTENT_LENGTH)) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("CONTENT_TOO_LONG", "内容长度不能超过" + MAX_POST_CONTENT_LENGTH + "个字符"));
        }

        // 从JWT过滤器中获取用户名
        String username = (String) httpRequest.getAttribute("username");

        try {
            Post post = postService.createPost(username, request);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("CREATE_POST_FAILED", "创建帖子失败：" + e.getMessage()));
        }
    }

    /**
     * 获取帖子列表（分页）
     */
    @GetMapping
    public ResponseEntity<?> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10; // 限制每页最多50条

        try {
            IPage<PostDTO.ListResponse> posts = postService.getPostList(page, size);

            // 转换为统一的分页响应格式
            PageResponse<PostDTO.ListResponse> response = PageResponse.fromIPage(posts);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("GET_POSTS_FAILED", "获取帖子列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取当前用户的帖子列表（分页）
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {

        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10; // 限制每页最多50条

        // 从JWT过滤器中获取用户名
        String username = (String) httpRequest.getAttribute("username");

        try {
            IPage<PostDTO.ListResponse> posts = postService.getMyPostList(username, page, size);

            // 转换为统一的分页响应格式
            PageResponse<PostDTO.ListResponse> response = PageResponse.fromIPage(posts);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("GET_MY_POSTS_FAILED", "获取我的帖子列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostDetail(@PathVariable Long postId) {

        try {
            PostDTO.DetailResponse post = postService.getPostDetail(postId);
            if (post == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("GET_POST_FAILED", "获取帖子详情失败：" + e.getMessage()));
        }
    }

    /**
     * 回复帖子
     */
    @PostMapping("/{postId}/replies")
    public ResponseEntity<?> replyToPost(@PathVariable Long postId,
                                         @RequestBody ReplyDTO.CreateRequest request,
                                         HttpServletRequest httpRequest) {

        // 参数验证
        if (request.getContent() == null || HtmlUtils.isContentEmpty(request.getContent())) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_CONTENT", "回复内容不能为空"));
        }

        if (HtmlUtils.isContentTooLong(request.getContent(), MAX_REPLY_CONTENT_LENGTH)) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("CONTENT_TOO_LONG", "回复内容长度不能超过" + MAX_REPLY_CONTENT_LENGTH + "个字符"));
        }

        // 检查帖子是否存在
        PostDTO.DetailResponse post = postService.getPostDetail(postId);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }

        // 从JWT过滤器中获取用户名
        String username = (String) httpRequest.getAttribute("username");

        try {
            Reply reply = replyService.createReply(postId, username, request);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("REPLY_FAILED", "回复失败：" + e.getMessage()));
        }
    }

    /**
     * 获取帖子的回复列表
     */
    @GetMapping("/{postId}/replies")
    public ResponseEntity<?> getReplies(@PathVariable Long postId) {

        // 检查帖子是否存在
        PostDTO.DetailResponse post = postService.getPostDetail(postId);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            java.util.List<ReplyDTO.Response> replies = replyService.getRepliesByPostId(postId);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("GET_REPLIES_FAILED", "获取回复列表失败：" + e.getMessage()));
        }
    }

    /**
     * 删除回复
     */
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<?> deleteReply(@PathVariable Long replyId,
                                         HttpServletRequest httpRequest) {

        // 从JWT过滤器中获取用户名
        String username = (String) httpRequest.getAttribute("username");

        try {
            boolean deleted = replyService.deleteReply(replyId, username);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(403)
                        .body(ErrorResponse.of("ACCESS_DENIED", "没有权限删除此回复或回复不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("DELETE_REPLY_FAILED", "删除回复失败：" + e.getMessage()));
        }
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");

        try {
            boolean deleted = postService.deletePost(postId, username);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(403)
                        .body(ErrorResponse.of("ACCESS_DENIED", "没有权限删除此帖子或帖子不存在"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("DELETE_POST_FAILED", "删除帖子失败：" + e.getMessage()));
        }
    }
}