package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.User;

public interface UserMapper extends BaseMapper<User> {
    // 如果需要复杂查询可以在这里自定义方法
}
