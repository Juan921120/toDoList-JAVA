package com.example.demo.web;

import com.example.demo.service.TaskService;
import com.example.demo.service.UserService;
import com.example.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.entity.TodoTask;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    /**
     * 从请求中获取当前登录用户的ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        if (username == null) {
            throw new RuntimeException("用户未登录");
        }
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user.getId();
    }

    // 获取当前用户的全部任务
    @GetMapping("/all")
    public List<TodoTask> getAll(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return taskService.lambdaQuery()
                .eq(TodoTask::getUserId, userId)
                // 按 id 倒序：最新插入的排最前
                .orderByDesc(TodoTask::getId)
                .list();
    }

    // 获取当前用户的已完成任务
    @GetMapping("/completed")
    public List<TodoTask> getCompleted(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return taskService.lambdaQuery()
                .eq(TodoTask::getUserId, userId)
                .eq(TodoTask::getStatus, true)
                .orderByDesc(TodoTask::getId)
                .list();
    }

    // 获取当前用户的未完成任务
    @GetMapping("/pending")
    public List<TodoTask> getPending(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return taskService.lambdaQuery()
                .eq(TodoTask::getUserId, userId)
                .eq(TodoTask::getStatus, false)
                .orderByDesc(TodoTask::getId)
                .list();
    }

    // 添加任务（自动关联到当前用户）
    @PostMapping("/add")
    public boolean addTask(@RequestBody TodoTask task, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);

        // 设置任务的用户信息
        task.setUserId(userId);

        return taskService.save(task);
    }

    // 单个任务标记为完成（只能操作自己的任务）
    @PutMapping("/complete/{id}")
    public boolean completeTask(@PathVariable Integer id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);

        // 先查询任务是否属于当前用户
        TodoTask existingTask = taskService.lambdaQuery()
                .eq(TodoTask::getId, id)
                .eq(TodoTask::getUserId, userId)
                .one();

        if (existingTask == null) {
            throw new RuntimeException("任务不存在或无权限操作");
        }

        TodoTask task = new TodoTask();
        task.setId(id);
        task.setStatus(true);
        return taskService.updateById(task);
    }

    // 批量完成任务（只能操作自己的任务）
    @PutMapping("/complete/batch")
    public boolean batchComplete(@RequestBody List<Integer> ids, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);

        // 验证所有任务都属于当前用户
        long count = taskService.lambdaQuery()
                .eq(TodoTask::getUserId, userId)
                .in(TodoTask::getId, ids)
                .count();

        if (count != ids.size()) {
            throw new RuntimeException("部分任务不存在或无权限操作");
        }

        List<TodoTask> updateList = ids.stream().map(id -> {
            TodoTask task = new TodoTask();
            task.setId(id);
            task.setStatus(true);
            return task;
        }).toList();

        return taskService.updateBatchById(updateList);
    }

    // 统计当前用户的任务
    @GetMapping("/count")
    public Map<String, Long> countAllStatus(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);

        long total = taskService.lambdaQuery()
                .eq(TodoTask::getUserId, userId)
                .count();

        long completed = taskService.lambdaQuery()
                .eq(TodoTask::getUserId, userId)
                .eq(TodoTask::getStatus, true)
                .count();

        long pending = total - completed;

        Map<String, Long> result = new HashMap<>();
        result.put("total", total);
        result.put("completed", completed);
        result.put("pending", pending);
        return result;
    }

    // 删除任务（只能删除自己的任务）
    @DeleteMapping("/delete/{id}")
    public boolean deleteTask(@PathVariable Integer id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);

        // 先查询任务是否属于当前用户
        TodoTask existingTask = taskService.lambdaQuery()
                .eq(TodoTask::getId, id)
                .eq(TodoTask::getUserId, userId)
                .one();

        if (existingTask == null) {
            throw new RuntimeException("任务不存在或无权限操作");
        }

        return taskService.removeById(id);
    }

    // 更新任务内容（只能更新自己的任务）
    @PutMapping("/update/{id}")
    public boolean updateTask(@PathVariable Integer id, @RequestBody TodoTask task, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);

        // 先查询任务是否属于当前用户
        TodoTask existingTask = taskService.lambdaQuery()
                .eq(TodoTask::getId, id)
                .eq(TodoTask::getUserId, userId)
                .one();

        if (existingTask == null) {
            throw new RuntimeException("任务不存在或无权限操作");
        }

        task.setId(id);
        task.setUserId(userId); // 确保用户ID不被篡改
        return taskService.updateById(task);
    }
}