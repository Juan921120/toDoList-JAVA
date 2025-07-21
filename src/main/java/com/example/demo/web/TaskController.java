package com.example.demo.web;

import com.example.demo.dto.ApiResponse;
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
    public ApiResponse<List<TodoTask>> getAll(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<TodoTask> tasks = taskService.lambdaQuery()
                    .eq(TodoTask::getUserId, userId)
                    // 按 id 倒序：最新插入的排最前
                    .orderByDesc(TodoTask::getId)
                    .list();
            return ApiResponse.ok("获取全部任务成功", tasks);
        } catch (Exception e) {
            return ApiResponse.fail("获取任务失败：" + e.getMessage());
        }
    }

    // 获取当前用户的已完成任务
    @GetMapping("/completed")
    public ApiResponse<List<TodoTask>> getCompleted(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<TodoTask> tasks = taskService.lambdaQuery()
                    .eq(TodoTask::getUserId, userId)
                    .eq(TodoTask::getStatus, true)
                    .orderByDesc(TodoTask::getId)
                    .list();
            return ApiResponse.ok("获取已完成任务成功", tasks);
        } catch (Exception e) {
            return ApiResponse.fail("获取任务失败：" + e.getMessage());
        }
    }

    // 获取当前用户的未完成任务
    @GetMapping("/pending")
    public ApiResponse<List<TodoTask>> getPending(HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            List<TodoTask> tasks = taskService.lambdaQuery()
                    .eq(TodoTask::getUserId, userId)
                    .eq(TodoTask::getStatus, false)
                    .orderByDesc(TodoTask::getId)
                    .list();
            return ApiResponse.ok("获取待完成任务成功", tasks);
        } catch (Exception e) {
            return ApiResponse.fail("获取任务失败：" + e.getMessage());
        }
    }

    // 添加任务（自动关联到当前用户）
    @PostMapping("/add")
    public ApiResponse<Boolean> addTask(@RequestBody TodoTask task, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);
            // 设置任务的用户信息
            task.setUserId(userId);
            boolean success = taskService.save(task);

            if (success) {
                return ApiResponse.ok("任务添加成功", true);
            } else {
                return ApiResponse.fail("任务添加失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail("添加任务失败：" + e.getMessage());
        }
    }

    // 单个任务标记为完成（只能操作自己的任务）
    @PutMapping("/complete/{id}")
    public ApiResponse<Boolean> completeTask(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);

            // 先查询任务是否属于当前用户
            TodoTask existingTask = taskService.lambdaQuery()
                    .eq(TodoTask::getId, id)
                    .eq(TodoTask::getUserId, userId)
                    .one();

            if (existingTask == null) {
                return ApiResponse.fail("任务不存在或无权限操作");
            }

            TodoTask task = new TodoTask();
            task.setId(id);
            task.setStatus(true);
            boolean success = taskService.updateById(task);

            if (success) {
                return ApiResponse.ok("任务完成成功", true);
            } else {
                return ApiResponse.fail("任务完成失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail("完成任务失败：" + e.getMessage());
        }
    }

    // 批量完成任务（只能操作自己的任务）
    @PutMapping("/complete/batch")
    public ApiResponse<Boolean> batchComplete(@RequestBody List<Integer> ids, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);

            // 验证所有任务都属于当前用户
            long count = taskService.lambdaQuery()
                    .eq(TodoTask::getUserId, userId)
                    .in(TodoTask::getId, ids)
                    .count();

            if (count != ids.size()) {
                return ApiResponse.fail("部分任务不存在或无权限操作");
            }

            List<TodoTask> updateList = ids.stream().map(id -> {
                TodoTask task = new TodoTask();
                task.setId(id);
                task.setStatus(true);
                return task;
            }).toList();

            boolean success = taskService.updateBatchById(updateList);

            if (success) {
                return ApiResponse.ok("批量完成任务成功", true);
            } else {
                return ApiResponse.fail("批量完成任务失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail("批量完成任务失败：" + e.getMessage());
        }
    }

    // 统计当前用户的任务
    @GetMapping("/count")
    public ApiResponse<Map<String, Long>> countAllStatus(HttpServletRequest request) {
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

            return ApiResponse.ok("获取统计数据成功", result);
        } catch (Exception e) {
            return ApiResponse.fail("获取统计数据失败：" + e.getMessage());
        }
    }

    // 删除任务（只能删除自己的任务）
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Boolean> deleteTask(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);

            // 先查询任务是否属于当前用户
            TodoTask existingTask = taskService.lambdaQuery()
                    .eq(TodoTask::getId, id)
                    .eq(TodoTask::getUserId, userId)
                    .one();

            if (existingTask == null) {
                return ApiResponse.fail("任务不存在或无权限操作");
            }

            boolean success = taskService.removeById(id);

            if (success) {
                return ApiResponse.ok("任务删除成功", true);
            } else {
                return ApiResponse.fail("任务删除失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail("删除任务失败：" + e.getMessage());
        }
    }

    // 更新任务内容（只能更新自己的任务）
    @PutMapping("/update/{id}")
    public ApiResponse<Boolean> updateTask(@PathVariable Integer id, @RequestBody TodoTask task, HttpServletRequest request) {
        try {
            Long userId = getCurrentUserId(request);

            // 先查询任务是否属于当前用户
            TodoTask existingTask = taskService.lambdaQuery()
                    .eq(TodoTask::getId, id)
                    .eq(TodoTask::getUserId, userId)
                    .one();

            if (existingTask == null) {
                return ApiResponse.fail("任务不存在或无权限操作");
            }

            task.setId(id);
            task.setUserId(userId); // 确保用户ID不被篡改
            boolean success = taskService.updateById(task);

            if (success) {
                return ApiResponse.ok("任务更新成功", true);
            } else {
                return ApiResponse.fail("任务更新失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail("更新任务失败：" + e.getMessage());
        }
    }
}