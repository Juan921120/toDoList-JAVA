package com.example.demo.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.ErrorResponse;
import com.example.demo.dto.PageResponse;
import com.example.demo.entity.TodoTask;
import com.example.demo.entity.User;
import com.example.demo.service.TaskService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
     * 从请求中获取当前用户（JWT Filter 已经验证过身份）
     */
    private User getCurrentUser(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        return userService.findByUsername(username);
    }

    /**
     * 分页获取待办任务
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingTasks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        User currentUser = getCurrentUser(request);

        Page<TodoTask> pageObj = new Page<>(page, pageSize);
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", currentUser.getId())
                .eq("status", false)
                .orderByDesc("id"); // 按创建时间倒序

        IPage<TodoTask> result = taskService.page(pageObj, wrapper);
        PageResponse<TodoTask> pageResponse = PageResponse.fromIPage(result);

        return ResponseEntity.ok(pageResponse);
    }

    /**
     * 分页获取已完成任务
     */
    @GetMapping("/completed")
    public ResponseEntity<?> getCompletedTasks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        User currentUser = getCurrentUser(request);

        Page<TodoTask> pageObj = new Page<>(page, pageSize);
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", currentUser.getId())
                .eq("status", true)
                .orderByDesc("id");

        IPage<TodoTask> result = taskService.page(pageObj, wrapper);
        PageResponse<TodoTask> pageResponse = PageResponse.fromIPage(result);

        return ResponseEntity.ok(pageResponse);
    }

    /**
     * 分页获取所有任务
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllTasks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {

        User currentUser = getCurrentUser(request);

        Page<TodoTask> pageObj = new Page<>(page, pageSize);
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", currentUser.getId())
                .orderByDesc("id");

        IPage<TodoTask> result = taskService.page(pageObj, wrapper);
        PageResponse<TodoTask> pageResponse = PageResponse.fromIPage(result);

        return ResponseEntity.ok(pageResponse);
    }

    /**
     * 获取任务统计
     */
    @GetMapping("/count")
    public ResponseEntity<?> getTaskCount(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);

        QueryWrapper<TodoTask> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", currentUser.getId());

        long total = taskService.count(wrapper);

        wrapper.eq("status", true);
        long completed = taskService.count(wrapper);

        long pending = total - completed;

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("completed", completed);
        result.put("pending", pending);
        result.put("percentage", total > 0 ? Math.round((completed * 100.0) / total) : 0);

        return ResponseEntity.ok(result);
    }

    /**
     * 添加任务
     */
    @PostMapping("/add")
    public ResponseEntity<?> addTask(@RequestBody Map<String, Object> taskData,
                                     HttpServletRequest request) {
        User currentUser = getCurrentUser(request);

        String text = (String) taskData.get("text");
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_TASK", "任务内容不能为空"));
        }

        try {
            TodoTask task = new TodoTask();
            task.setText(text.trim());
            task.setStatus(false);
            task.setUserId(currentUser.getId());

            boolean success = taskService.save(task);
            if (success) {
                return ResponseEntity.ok(Map.of("message", "添加成功", "taskId", task.getId()));
            } else {
                return ResponseEntity.internalServerError()
                        .body(ErrorResponse.of("SAVE_FAILED", "保存任务失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("SERVER_ERROR", "服务器错误：" + e.getMessage()));
        }
    }

    /**
     * 完成单个任务
     */
    @PutMapping("/complete/{id}")
    public ResponseEntity<?> completeTask(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = getCurrentUser(request);

        TodoTask task = taskService.getById(id);
        if (task == null) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("TASK_NOT_FOUND", "任务不存在"));
        }

        // 检查任务所有权
        if (!task.getUserId().equals(currentUser.getId())) {
            return ResponseEntity.status(403)
                    .body(ErrorResponse.of("FORBIDDEN", "无权操作此任务"));
        }

        task.setStatus(true);
        boolean success = taskService.updateById(task);

        if (success) {
            return ResponseEntity.ok(Map.of("message", "任务已完成"));
        } else {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("UPDATE_FAILED", "更新任务失败"));
        }
    }

    /**
     * 批量完成任务
     */
    @PutMapping("/complete/batch")
    public ResponseEntity<?> batchCompleteTask(@RequestBody List<Long> taskIds,
                                               HttpServletRequest request) {
        User currentUser = getCurrentUser(request);

        if (taskIds == null || taskIds.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_TASK_IDS", "任务ID列表不能为空"));
        }

        // 查询要更新的任务，确保都属于当前用户
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<>();
        wrapper.in("id", taskIds).eq("user_id", currentUser.getId());
        List<TodoTask> tasks = taskService.list(wrapper);

        if (tasks.size() != taskIds.size()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_TASKS", "部分任务不存在或无权操作"));
        }

        // 批量更新状态
        tasks.forEach(task -> task.setStatus(true));
        boolean success = taskService.updateBatchById(tasks);

        if (success) {
            return ResponseEntity.ok(Map.of("message", "批量完成成功", "count", tasks.size()));
        } else {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("UPDATE_FAILED", "批量更新失败"));
        }
    }

    /**
     * 删除单个任务
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = getCurrentUser(request);

        TodoTask task = taskService.getById(id);
        if (task == null) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("TASK_NOT_FOUND", "任务不存在"));
        }

        // 检查任务所有权
        if (!task.getUserId().equals(currentUser.getId())) {
            return ResponseEntity.status(403)
                    .body(ErrorResponse.of("FORBIDDEN", "无权操作此任务"));
        }

        boolean success = taskService.removeById(id);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "删除成功"));
        } else {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("DELETE_FAILED", "删除任务失败"));
        }
    }

    /**
     * 批量删除任务
     */
    @DeleteMapping("/batch")
    public ResponseEntity<?> batchDeleteTask(@RequestBody List<Long> taskIds,
                                             HttpServletRequest request) {
        User currentUser = getCurrentUser(request);

        if (taskIds == null || taskIds.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_TASK_IDS", "任务ID列表不能为空"));
        }

        // 查询要删除的任务，确保都属于当前用户
        QueryWrapper<TodoTask> wrapper = new QueryWrapper<>();
        wrapper.in("id", taskIds).eq("user_id", currentUser.getId());
        List<TodoTask> tasks = taskService.list(wrapper);

        if (tasks.size() != taskIds.size()) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_TASKS", "部分任务不存在或无权操作"));
        }

        boolean success = taskService.removeByIds(taskIds);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "批量删除成功", "count", tasks.size()));
        } else {
            return ResponseEntity.internalServerError()
                    .body(ErrorResponse.of("DELETE_FAILED", "批量删除失败"));
        }
    }
}