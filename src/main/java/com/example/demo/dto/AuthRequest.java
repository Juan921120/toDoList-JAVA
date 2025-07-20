package com.example.demo.dto;

// AuthRequest.java

import lombok.Data;

@Data
public class AuthRequest { //接收前端发来的登录请求数据
    private String username;
    private String password;
    // getters & setters
}

