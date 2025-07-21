package com.example.demo.web;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.TodoTask;
import com.example.demo.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController  // 添加这个注解！
public class Debug {
    @Autowired
    private TaskService taskService;
    @GetMapping("/debug")
    public ApiResponse<List<TodoTask>> debug(HttpServletRequest request) {
        try {
            // 直接用数据库中存在的用户ID
            Long userId = 8L;
            System.out.println("直接查询用户ID=8的任务");

            List<TodoTask> tasks = taskService.lambdaQuery()
                    .eq(TodoTask::getUserId, userId)
                    .orderByDesc(TodoTask::getId)
                    .list();

            System.out.println("直接查询到的任务数量: " + tasks.size());

            return ApiResponse.ok("调试查询成功", tasks);
        } catch (Exception e) {
            return ApiResponse.fail("调试查询失败：" + e.getMessage());
        }
    }
}
