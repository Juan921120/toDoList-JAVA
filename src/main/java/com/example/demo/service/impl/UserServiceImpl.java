package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User findByUsername(String username) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .one();
    }



    @Override
    public User register(String username, String rawPassword) {
        String hash = passwordEncoder.encode(rawPassword);
        User u = new User();
        u.setUsername(username);
        u.setPassword(hash);
        // u.setRoles("ROLE_USER");
        save(u);
        return u;
    }

    @Override
    public void update(User u) {

    }
}
