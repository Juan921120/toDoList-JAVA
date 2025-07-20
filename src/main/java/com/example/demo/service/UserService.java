package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.User;

import java.util.List;

public interface UserService extends IService<User> {
    User findByUsername(String username);


    User register(String username, String rawPassword);

    void update(User u);
}
