package com.zxinfotek.tms.admin.web;

import com.zxinfotek.tms.common.exception.AuthException;
import com.zxinfotek.tms.common.exception.BizException;
import com.zxinfotek.tms.common.exception.CommonErrorCode;
import com.zxinfotek.tms.common.exception.ConflictException;
import com.zxinfotek.tms.common.exception.NotFoundException;
import com.zxinfotek.tms.common.exception.PermissionException;
import com.zxinfotek.tms.common.model.Result;
import com.zxinfotek.tms.infra.context.RequestContextHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理：统一包装为标准响应体，任何情况下不把堆栈、SQL 或内部路径返回给客户端（详细设计 7.4）。
 *
 * @author zxinfotek
 * @since 2026-09-18
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Object>> handleBiz(BizException e, HttpServletResponse response) {
        // 依赖不可用按 503 返回，其余业务校验失败按 400（详细设计 5.2、9.2）
        HttpStatus status = CommonErrorCode.COMMON_004.getCode().equals(e.getErrorCode().getCode())
                ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.BAD_REQUEST;
        return respond(status, e.getErrorCode().getCode(), e.getMessage(), e.getDetail(), response);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Result<Object>> handleAuth(AuthException e, HttpServletResponse response) {
        return respond(HttpStatus.UNAUTHORIZED, e.getErrorCode().getCode(), e.getMessage(), null, response);
    }

    @ExceptionHandler(PermissionException.class)
    public ResponseEntity<Result<Object>> handlePermission(PermissionException e,
                                                           HttpServletResponse response) {
        return respond(HttpStatus.FORBIDDEN, e.getErrorCode().getCode(), e.getMessage(),
                e.getDetail(), response);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Result<Object>> handleNotFound(NotFoundException e, HttpServletResponse response) {
        return respond(HttpStatus.NOT_FOUND, e.getErrorCode().getCode(), e.getMessage(), null, response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Result<Object>> handleConflict(ConflictException e, HttpServletResponse response) {
        return respond(HttpStatus.CONFLICT, e.getErrorCode().getCode(), e.getMessage(), null, response);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Result<Object>> handleValidation(BindException e, HttpServletResponse response) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? CommonErrorCode.COMMON_001.getMessage()
                : fieldError.getDefaultMessage();
        return respond(HttpStatus.BAD_REQUEST, CommonErrorCode.COMMON_001.getCode(), message, null, response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                                             HttpServletResponse response) {
        return respond(HttpStatus.BAD_REQUEST, CommonErrorCode.COMMON_001.getCode(),
                "参数 " + e.getName() + " 取值不合法", null, response);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Object>> handleDuplicateKey(DuplicateKeyException e,
                                                             HttpServletResponse response) {
        log.warn("唯一约束冲突", e);
        return respond(HttpStatus.CONFLICT, CommonErrorCode.COMMON_002.getCode(),
                "数据已存在或已被其他人修改，请刷新后重试", null, response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleUnexpected(Exception e, HttpServletResponse response) {
        log.error("未捕获异常，traceId={}", RequestContextHolder.get().getTraceId(), e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, CommonErrorCode.COMMON_005.getCode(),
                CommonErrorCode.COMMON_005.getMessage(), null, response);
    }

    private ResponseEntity<Result<Object>> respond(HttpStatus status, String code, String message,
                                                   Object detail, HttpServletResponse response) {
        Result<Object> result = new Result<>(code, message, detail);
        result.setTraceId(RequestContextHolder.get().getTraceId());
        response.setHeader("X-Biz-Code", code);
        return ResponseEntity.status(status).body(result);
    }
}
