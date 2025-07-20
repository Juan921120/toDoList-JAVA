package com.example.demo.web;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.AuthRequest;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;                // 自己写的查询用户的 Service

    @Autowired
    private JwtUtil jwtUtil;                        // 生成 JWT 的工具类

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String,Object>>> login(
            @RequestBody AuthRequest req) {

        // 1. 根据用户名查用户
        User user = userService.findByUsername(req.getUsername());
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("用户不存在"));
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("用户名或密码错误"));
        }

        // 3. 生成 Token
        String token = jwtUtil.generateToken(user.getUsername());

        // 4. 构造返回数据
        Map<String,Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        // 如果需要，可以把用户角色、头像 URL 等一起返回：
        // data.put("roles", user.getRoles());

        // 5. 返回
        ApiResponse<Map<String,Object>> resp = ApiResponse.ok("登录成功", data);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String,Object>>> register(
            @RequestBody AuthRequest req) {

        // 1. 参数验证
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("用户名不能为空"));
        }

        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("密码不能为空"));
        }

        // 2. 检查用户名是否已存在
        User existingUser = userService.findByUsername(req.getUsername());
        if (existingUser != null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.fail("用户名已存在"));
        }

        // 3. 创建用户
        try {
            User newUser = userService.register(req.getUsername(), req.getPassword());

            // 4. 生成 Token (注册成功后自动登录)
            String token = jwtUtil.generateToken(newUser.getUsername());

            // 5. 构造返回数据
            Map<String,Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", newUser.getUsername());
            data.put("userId", newUser.getId());

            // 6. 返回
            ApiResponse<Map<String,Object>> resp = ApiResponse.ok("注册成功", data);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("注册失败：" + e.getMessage()));
        }
    }
}
