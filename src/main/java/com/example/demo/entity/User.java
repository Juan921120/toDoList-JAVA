package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")  // 对应数据库表名
public class User {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    // 存储 BCrypt 加密后的密码
    private String password;


}