package com.example.demo.web;
import com.example.demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.entity.TodoTask;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    private TaskService taskService;

    // 获取全部任务
    @GetMapping("/all")
    public List<TodoTask> getAll() {
        return taskService.lambdaQuery()
                // 按 id 倒序：最新插入的排最前
                .orderByDesc(TodoTask::getId)
                .list();
    }

    // 获取已完成任务
    @GetMapping("/completed")
    public List<TodoTask> getCompleted() {
        return taskService.lambdaQuery().eq(TodoTask::getStatus, true).list();
    }

    // 获取未完成任务
    @GetMapping("/pending")
    public List<TodoTask> getPending() {
        return taskService.lambdaQuery().eq(TodoTask::getStatus, false).list();
    }

    // 添加任务
    @PostMapping("/add")
    public boolean addTask(@RequestBody TodoTask task) {
        return taskService.save(task);
    }

    // 单个任务标记为完成
    @PutMapping("/complete/{id}")
    public boolean completeTask(@PathVariable Integer id) {
        TodoTask task = new TodoTask();
        task.setId(id);
        task.setStatus(true);
        return taskService.updateById(task);
    }

    // 批量完成任务
    @PutMapping("/complete/batch")
    public boolean batchComplete(@RequestBody List<Integer> ids) {
        List<TodoTask> updateList = ids.stream().map(id -> {
            TodoTask task = new TodoTask();
            task.setId(id);
            task.setStatus(true);
            return task;
        }).toList();

        return taskService.updateBatchById(updateList);
    }

    //统计
    @GetMapping("/count")
    public Map<String, Long> countAllStatus() {
        long total = taskService.count();
        long completed = taskService.lambdaQuery().eq(TodoTask::getStatus, true).count();
        long pending = total - completed;

        Map<String, Long> result = new HashMap<>();
        result.put("total", total);
        result.put("completed", completed);
        result.put("pending", pending);
        return result;
    }
}
