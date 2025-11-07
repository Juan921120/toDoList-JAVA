package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName( "task")
public class TodoTask {
    @TableId // 主键
    private Integer id;
    @TableField("task_text") // 指定数据库字段名
    private  String text;
    private Boolean status;

    // 新增：关联用户字段，使用BIGINT类型
    @TableField("user_id")
    private Long userId;

    // 新增字段
    private Integer progress;     // 任务进度（0-100）
    @TableField("order_index")
    private Integer orderIndex;   // 任务顺序索引


}
