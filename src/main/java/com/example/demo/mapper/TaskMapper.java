package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.example.demo.entity.TodoTask;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


public interface TaskMapper extends BaseMapper<TodoTask> {



}
