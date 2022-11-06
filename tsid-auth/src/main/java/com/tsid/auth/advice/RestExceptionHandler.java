package com.tsid.auth.advice;

import com.tsid.auth.ResValue;
import com.tsid.auth.common.Constants;
import com.tsid.auth.exception.AuthServerException;
import com.tsid.auth.exception.ErrCode;
import com.tsid.auth.service.ErrorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private final ErrorService errorService;

    @ExceptionHandler(AuthServerException.class)
    protected ResponseEntity<?> handleCustomException(AuthServerException e) {
        log.error("AuthServerException", e);
        ErrCode errorCode = e.getErrCode();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(e.getMessage())
                .code(errorCode.getCode())
                .type("POPUP")
                .build();

        if (errorCode.getStatus() == 200) {
            return new ResponseEntity<>(new ResValue<>(errorResponse), HttpStatus.resolve(errorCode.getStatus()));
        } else {
            return new ResponseEntity<>(errorResponse, HttpStatus.resolve(errorCode.getStatus()));
        }
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> customException(Exception e, HttpServletRequest request) {

        String message = e.getMessage();
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            String track = stackTraceElement.toString();
            if (track.contains(Constants.SERVER_PACKAGE)) {
                message += "\n" + track;
            }
        }

        String platform = request.getHeader("platform");
        String version = request.getHeader("version");
        errorService.insertErrorLog(platform, version, request.getRequestURI(), message);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(e.getMessage())
                .code("500")
                .type("NONE")
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
