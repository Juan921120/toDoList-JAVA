package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Post;
import org.apache.ibatis.annotations.Update;

public interface PostMapper extends BaseMapper<Post> {

    /**
     * 增加帖子回复数量
     */
    @Update("UPDATE post SET reply_count = reply_count + 1, last_reply_time = NOW() WHERE id = #{postId}")
    void incrementReplyCount(Long postId);

    /**
     * 减少帖子回复数量
     */
    @Update("UPDATE post SET reply_count = reply_count - 1 WHERE id = #{postId} AND reply_count > 0")
    void decrementReplyCount(Long postId);
}