
package com.example.demo.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @param <T> data 类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    /** 操作是否成功 */
    private boolean success;
    /** 返回给前端的提示信息 */
    private String message;
    /** 具体数据载体，查询列表、单体对象、分页数据等 */
    private T data;

    /** 快速构造无 data 的成功响应 */
    public static <T> ApiResponse<T> ok(String msg) {
        return new ApiResponse<>(true, msg, null);
    }
    /** 快速构造带 data 的成功响应 */
    public static <T> ApiResponse<T> ok(String msg, T data) {
        return new ApiResponse<>(true, msg, data);
    }
    /** 快速构造失败响应 */
    public static <T> ApiResponse<T> fail(String msg) {
        return new ApiResponse<>(false, msg, null);
    }
}
