package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.TodoTask;
import com.example.demo.mapper.TaskMapper;
import com.example.demo.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, TodoTask> implements TaskService {

    @Override
    public boolean updateTask(TodoTask task) {
        return updateById(task);
    }

    /**
     * 置顶任务：把该任务的 order_index 设为最小值，其他任务顺序依次往后排
     * 修复版本：参数类型改为 Integer，逻辑更清晰
     */
    @Override
    @Transactional
    public void pinTask(Integer taskId) {
        TodoTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 获取当前用户的所有任务，按order_index排序
        List<TodoTask> allTasks = lambdaQuery()
                .eq(TodoTask::getUserId, task.getUserId())
                .orderByAsc(TodoTask::getOrderIndex)
                .list();

        if (allTasks.isEmpty()) {
            return;
        }

        // 找到要置顶的任务在列表中的位置
        int targetIndex = -1;
        for (int i = 0; i < allTasks.size(); i++) {
            if (allTasks.get(i).getId().equals(taskId)) {  // 现在类型匹配了
                targetIndex = i;
                break;
            }
        }

        if (targetIndex == -1) {
            throw new RuntimeException("任务不存在于用户任务列表中");
        }

        // 如果任务已经在第一位，无需操作
        if (targetIndex == 0) {
            return;
        }

        // 重新分配所有任务的order_index
        // 置顶任务设为1，其他任务依次递增
        TodoTask pinnedTask = allTasks.get(targetIndex);
        pinnedTask.setOrderIndex(1);

        int newIndex = 2;
        for (int i = 0; i < allTasks.size(); i++) {
            if (i != targetIndex) {
                allTasks.get(i).setOrderIndex(newIndex++);
            }
        }

        // 批量更新
        boolean success = updateBatchById(allTasks);
        if (!success) {
            throw new RuntimeException("置顶操作失败");
        }
    }

    /**
     * 上移任务：修复版本，参数类型改为 Integer
     */
    @Override
    @Transactional
    public void moveTaskUp(Integer taskId) {
        TodoTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 获取当前用户的所有任务，按order_index排序
        List<TodoTask> allTasks = lambdaQuery()
                .eq(TodoTask::getUserId, task.getUserId())
                .orderByAsc(TodoTask::getOrderIndex)
                .list();

        // 找到当前任务的位置
        int currentIndex = -1;
        for (int i = 0; i < allTasks.size(); i++) {
            if (allTasks.get(i).getId().equals(taskId)) {  // 现在类型匹配了
                currentIndex = i;
                break;
            }
        }

        // 如果任务已经在最前面，无法上移
        if (currentIndex <= 0) {
            return;
        }

        // 与前一个任务交换位置
        TodoTask currentTask = allTasks.get(currentIndex);
        TodoTask previousTask = allTasks.get(currentIndex - 1);

        int tempOrder = currentTask.getOrderIndex();
        currentTask.setOrderIndex(previousTask.getOrderIndex());
        previousTask.setOrderIndex(tempOrder);

        // 更新两个任务
        boolean success = updateBatchById(List.of(currentTask, previousTask));
        if (!success) {
            throw new RuntimeException("上移操作失败");
        }
    }

    /**
     * 下移任务：修复版本，参数类型改为 Integer
     */
    @Override
    @Transactional
    public void moveTaskDown(Integer taskId) {
        TodoTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 获取当前用户的所有任务，按order_index排序
        List<TodoTask> allTasks = lambdaQuery()
                .eq(TodoTask::getUserId, task.getUserId())
                .orderByAsc(TodoTask::getOrderIndex)
                .list();

        // 找到当前任务的位置
        int currentIndex = -1;
        for (int i = 0; i < allTasks.size(); i++) {
            if (allTasks.get(i).getId().equals(taskId)) {  // 现在类型匹配了
                currentIndex = i;
                break;
            }
        }

        // 如果任务已经在最后面，无法下移
        if (currentIndex == -1 || currentIndex >= allTasks.size() - 1) {
            return;
        }

        // 与后一个任务交换位置
        TodoTask currentTask = allTasks.get(currentIndex);
        TodoTask nextTask = allTasks.get(currentIndex + 1);

        int tempOrder = currentTask.getOrderIndex();
        currentTask.setOrderIndex(nextTask.getOrderIndex());
        nextTask.setOrderIndex(tempOrder);

        // 更新两个任务
        boolean success = updateBatchById(List.of(currentTask, nextTask));
        if (!success) {
            throw new RuntimeException("下移操作失败");
        }
    }
}