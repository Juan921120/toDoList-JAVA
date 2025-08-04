package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    /**
     * 数据列表
     */
    private List<T> data;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrev;

    /**
     * 构造方法
     */
    public PageResponse(List<T> data, Long total, Integer page, Integer pageSize) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
        this.hasNext = page < totalPages;
        this.hasPrev = page > 1;
    }

    /**
     * 静态构造方法
     */
    public static <T> PageResponse<T> of(List<T> data, Long total, Integer page, Integer pageSize) {
        return new PageResponse<>(data, total, page, pageSize);
    }

    /**
     * 从MyBatis Plus的IPage转换
     */
    public static <T> PageResponse<T> fromIPage(com.baomidou.mybatisplus.core.metadata.IPage<T> iPage) {
        return new PageResponse<>(
                iPage.getRecords(),
                iPage.getTotal(),
                (int) iPage.getCurrent(),
                (int) iPage.getSize()
        );
    }
}