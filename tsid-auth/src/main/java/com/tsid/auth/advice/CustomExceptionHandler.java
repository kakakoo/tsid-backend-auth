package com.tsid.auth.advice;

import com.tsid.auth.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ErrorResponse customException(CustomException ex, HttpServletResponse response) {
        try {
            response.setStatus(470);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(ex.getMessage())
                .code("470")
                .type("NONE")
                .build();
        return errorResponse;
    }
}
