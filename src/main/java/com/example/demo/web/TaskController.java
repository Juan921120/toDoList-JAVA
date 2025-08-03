package com.example.demo.web;

import com.example.demo.dto.ErrorResponse;  // 新增导入
import com.example.demo.service.TaskService;
import com.example.demo.service.UserService;
import com.example.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.entity.TodoTask;
import org.springframework.http.ResponseEntity;  // 新增导入
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
    public ResponseEntity<?> getAll(HttpServletRequest request) {  // 修改返回类型
        try {
            Long userId = getCurrentUserId(request);
            List<TodoTask> tasks = taskService.lambdaQuery()
                    .eq(TodoTask::getUserId, userId)
                    .orderByDesc(TodoTask::getId)
                    .list();
            return ResponseEntity.ok(tasks);  // 修改返回方式
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("GET_TASKS_FAILED", "获取任务失败：" + e.getMessage()));
        }
    }

    // 获取当前用户的已完成任务
    @GetMapping("/completed")
    public ResponseEntity<?> getCompleted(HttpServletRequest request) {  // 修改返回类型
        try {
            Long userId = getCurrentUserId(request);
            List<TodoTask> tasks = taskService.lambdaQuery()
                    .eq(TodoTask::getUserId, userId)
                    .eq(TodoTask::getStatus, true)
                    .orderByDesc(TodoTask::getId)
                    .list();
            return ResponseEntity.ok(tasks);  // 修改返回方式
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("GET_TASKS_FAILED", "获取任务失败：" + e.getMessage()));
        }
    }

    // 获取当前用户的未完成任务
    @GetMapping("/pending")
    public ResponseEntity<?> getPending(HttpServletRequest request) {  // 修改返回类型
        try {
            Long userId = getCurrentUserId(request);
            List<TodoTask> tasks = taskService.lambdaQuery()
                    .eq(TodoTask::getUserId, userId)
                    .eq(TodoTask::getStatus, false)
                    .orderByDesc(TodoTask::getId)
                    .list();
            return ResponseEntity.ok(tasks);  // 修改返回方式
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("GET_TASKS_FAILED", "获取任务失败：" + e.getMessage()));
        }
    }

    // 添加任务（自动关联到当前用户）
    @PostMapping("/add")
    public ResponseEntity<?> addTask(@RequestBody TodoTask task, HttpServletRequest request) {  // 修改返回类型
        try {
            Long userId = getCurrentUserId(request);
            // 设置任务的用户信息
            task.setUserId(userId);
            boolean success = taskService.save(task);

            if (success) {
                return ResponseEntity.ok(task);  // 修改返回方式 - 返回创建的任务
            } else {
                return ResponseEntity.internalServerError()  // 修改返回方式
                        .body(ErrorResponse.of("ADD_TASK_FAILED", "任务添加失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("ADD_TASK_FAILED", "添加任务失败：" + e.getMessage()));
        }
    }

    // 单个任务标记为完成（只能操作自己的任务）
    @PutMapping("/complete/{id}")
    public ResponseEntity<?> completeTask(@PathVariable Integer id, HttpServletRequest request) {  // 修改返回类型
        try {
            Long userId = getCurrentUserId(request);

            // 先查询任务是否属于当前用户
            TodoTask existingTask = taskService.lambdaQuery()
                    .eq(TodoTask::getId, id)
                    .eq(TodoTask::getUserId, userId)
                    .one();

            if (existingTask == null) {
                return ResponseEntity.status(403)  // 修改返回方式 - 403 Forbidden
                        .body(ErrorResponse.of("ACCESS_DENIED", "任务不存在或无权限操作"));
            }

            TodoTask task = new TodoTask();
            task.setId(id);
            task.setStatus(true);
            boolean success = taskService.updateById(task);

            if (success) {
                return ResponseEntity.noContent().build();  // 修改返回方式 - 204 No Content
            } else {
                return ResponseEntity.internalServerError()  // 修改返回方式
                        .body(ErrorResponse.of("UPDATE_TASK_FAILED", "任务完成失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("UPDATE_TASK_FAILED", "完成任务失败：" + e.getMessage()));
        }
    }

    // 批量完成任务（只能操作自己的任务）
    @PutMapping("/complete/batch")
    public ResponseEntity<?> batchComplete(@RequestBody List<Integer> ids, HttpServletRequest request) {  // 修改返回类型
        try {
            Long userId = getCurrentUserId(request);

            // 验证所有任务都属于当前用户
            long count = taskService.lambdaQuery()
                    .eq(TodoTask::getUserId, userId)
                    .in(TodoTask::getId, ids)
                    .count();

            if (count != ids.size()) {
                return ResponseEntity.status(403)  // 修改返回方式 - 403 Forbidden
                        .body(ErrorResponse.of("ACCESS_DENIED", "部分任务不存在或无权限操作"));
            }

            List<TodoTask> updateList = ids.stream().map(id -> {
                TodoTask task = new TodoTask();
                task.setId(id);
                task.setStatus(true);
                return task;
            }).toList();

            boolean success = taskService.updateBatchById(updateList);

            if (success) {
                return ResponseEntity.noContent().build();  // 修改返回方式 - 204 No Content
            } else {
                return ResponseEntity.internalServerError()  // 修改返回方式
                        .body(ErrorResponse.of("BATCH_UPDATE_FAILED", "批量完成任务失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("BATCH_UPDATE_FAILED", "批量完成任务失败：" + e.getMessage()));
        }
    }

    // 统计当前用户的任务
    @GetMapping("/count")
    public ResponseEntity<?> countAllStatus(HttpServletRequest request) {  // 修改返回类型
        try {
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

            return ResponseEntity.ok(result);  // 修改返回方式
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("COUNT_FAILED", "获取统计数据失败：" + e.getMessage()));
        }
    }

    // 删除任务（只能删除自己的任务）
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer id, HttpServletRequest request) {  // 修改返回类型
        try {
            Long userId = getCurrentUserId(request);

            // 先查询任务是否属于当前用户
            TodoTask existingTask = taskService.lambdaQuery()
                    .eq(TodoTask::getId, id)
                    .eq(TodoTask::getUserId, userId)
                    .one();

            if (existingTask == null) {
                return ResponseEntity.status(403)  // 修改返回方式 - 403 Forbidden
                        .body(ErrorResponse.of("ACCESS_DENIED", "任务不存在或无权限操作"));
            }

            boolean success = taskService.removeById(id);

            if (success) {
                return ResponseEntity.noContent().build();  // 修改返回方式 - 204 No Content
            } else {
                return ResponseEntity.internalServerError()  // 修改返回方式
                        .body(ErrorResponse.of("DELETE_TASK_FAILED", "任务删除失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("DELETE_TASK_FAILED", "删除任务失败：" + e.getMessage()));
        }
    }

    // 更新任务内容（只能更新自己的任务）
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer id, @RequestBody TodoTask task, HttpServletRequest request) {  // 修改返回类型
        try {
            Long userId = getCurrentUserId(request);

            // 先查询任务是否属于当前用户
            TodoTask existingTask = taskService.lambdaQuery()
                    .eq(TodoTask::getId, id)
                    .eq(TodoTask::getUserId, userId)
                    .one();

            if (existingTask == null) {
                return ResponseEntity.status(403)  // 修改返回方式 - 403 Forbidden
                        .body(ErrorResponse.of("ACCESS_DENIED", "任务不存在或无权限操作"));
            }

            task.setId(id);
            task.setUserId(userId); // 确保用户ID不被篡改
            boolean success = taskService.updateById(task);

            if (success) {
                return ResponseEntity.ok(task);  // 修改返回方式 - 返回更新后的任务
            } else {
                return ResponseEntity.internalServerError()  // 修改返回方式
                        .body(ErrorResponse.of("UPDATE_TASK_FAILED", "任务更新失败"));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()  // 修改返回方式
                    .body(ErrorResponse.of("UPDATE_TASK_FAILED", "更新任务失败：" + e.getMessage()));
        }
    }
}