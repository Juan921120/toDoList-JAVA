package com.example.demo.web;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.ErrorResponse;  // 新增导入
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;  // 新增导入
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
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req) {  // 修改返回类型

        // 1. 根据用户名查用户
        User user = userService.findByUsername(req.getUsername());
        if (user == null) {
            return ResponseEntity.badRequest()  // 修改返回方式
                    .body(ErrorResponse.of("USER_NOT_FOUND", "用户不存在"));
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest()  // 修改返回方式
                    .body(ErrorResponse.of("INVALID_CREDENTIALS", "用户名或密码错误"));
        }

        // 3. 生成 Token
        String token = jwtUtil.generateToken(user.getUsername());

        // 4. 构造返回数据
        Map<String,Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());

        // 5. 返回
        return ResponseEntity.ok(data);  // 修改返回方式
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req) {  // 修改返回类型

        // 1. 参数验证
        if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest()  // 修改返回方式
                    .body(ErrorResponse.of("INVALID_USERNAME", "用户名不能为空"));
        }

        if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest()  // 修改返回方式
                    .body(ErrorResponse.of("INVALID_PASSWORD", "密码不能为空"));
        }

        // 2. 检查用户名是否已存在
        User existingUser = userService.findByUsername(req.getUsername());
        if (existingUser != null) {
            return ResponseEntity.badRequest()  // 修改返回方式
                    .body(ErrorResponse.of("USERNAME_EXISTS", "用户名已存在"));
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
            return ResponseEntity.ok(data);  // 修改返回方式

        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("REGISTER_FAILED", "注册失败：" + e.getMessage()));
        }
    }
}