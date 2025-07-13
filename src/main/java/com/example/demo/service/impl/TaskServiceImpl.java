package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.TaskMapper;
import com.example.demo.service.TaskService;
import com.example.demo.entity.TodoTask;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, TodoTask> implements TaskService {

}
