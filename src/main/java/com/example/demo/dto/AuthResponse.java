package com.example.demo.dto;

import lombok.Data;

@Data
//封装后端返回给前端的登录结果数据
public class AuthResponse {
    private String token;
    private String username;
    private String message;

    public AuthResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }
    public AuthResponse(String message) {
        this.message = message;
    }
}
