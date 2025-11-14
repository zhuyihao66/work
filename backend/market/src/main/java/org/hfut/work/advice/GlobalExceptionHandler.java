package org.hfut.work.advice;

import org.hfut.work.common.ApiResponse;
import org.hfut.work.exception.BusinessException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResponse<Void> handleValidation(Exception ex) {
        String msg;
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException manve = (MethodArgumentNotValidException) ex;
            msg = manve.getBindingResult().getFieldErrors().stream().findFirst()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .orElse("参数校验失败");
        } else {
            BindException be = (BindException) ex;
            msg = be.getBindingResult().getFieldErrors().stream().findFirst()
                    .map(e -> e.getField() + ": " + e.getDefaultMessage())
                    .orElse("参数校验失败");
        }
        return ApiResponse.error(400, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraint(ConstraintViolationException ex) {
        String msg = ex.getConstraintViolations().stream().findFirst()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .orElse("参数不合法");
        return ApiResponse.error(400, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleBadBody(HttpMessageNotReadableException ex) {
        return ApiResponse.error(400, "请求体格式错误");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIAE(IllegalArgumentException ex) {
        return ApiResponse.error(400, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ApiResponse<Void> handleISE(IllegalStateException ex) {
        return ApiResponse.error(409, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOther(Exception ex) {
        return ApiResponse.error(500, "服务器内部错误");
    }
}


