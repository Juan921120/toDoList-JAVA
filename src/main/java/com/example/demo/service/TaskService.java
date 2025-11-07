package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.TodoTask;

public interface TaskService extends IService<TodoTask> {

    boolean updateTask(TodoTask task);

    // 修改参数类型为 Integer，与 TodoTask.id 类型保持一致
    void pinTask(Integer taskId);

    void moveTaskUp(Integer taskId);

    void moveTaskDown(Integer taskId);
}