package com.storage073.exception;

import com.storage073.entity.vo.ResponseVO;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = { IllegalArgumentException.class })
    public ResponseVO<?> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return ResponseVO.failure(400, "Invalid argument: " + ex.getMessage());
    }

    @ExceptionHandler(value = { NullPointerException.class })
    public ResponseVO<?> handleNullPointerException(NullPointerException ex, WebRequest request) {
        return ResponseVO.failure(500, "Null pointer exception occurred. This may indicate a bug in the code.");
    }

    @ExceptionHandler(value = { Exception.class })
    public ResponseVO<?> handleGeneralException(Exception ex, WebRequest request) {
        return ResponseVO.failure(500, "An unexpected error occurred: " + ex.getMessage());
    }
}
