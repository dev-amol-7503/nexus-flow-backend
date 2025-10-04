package com.nexus_flow.nexus_flow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;
    private Integer statusCode;

    // Success responses
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now().toString(), HttpStatus.OK.value());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now().toString(), HttpStatus.OK.value());
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now().toString(), HttpStatus.CREATED.value());
    }

    // Error responses
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now().toString(), HttpStatus.BAD_REQUEST.value());
    }

    public static <T> ApiResponse<T> error(String message, Integer statusCode) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now().toString(), statusCode);
    }
}