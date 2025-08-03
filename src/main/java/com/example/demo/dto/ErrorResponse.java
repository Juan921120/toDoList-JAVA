package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String error;
    private String message;

    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message);
    }

    public static ErrorResponse of(String message) {
        return new ErrorResponse("BAD_REQUEST", message);
    }
}