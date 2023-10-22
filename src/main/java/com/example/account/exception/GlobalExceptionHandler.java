package com.example.account.exception;

import com.example.account.dto.ErrorResponse;
import com.example.account.type.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AccountException.class)
    public ErrorResponse handleAccountException(AccountException e) {
        log.error("{} is occurred", e.getErrorCode());

        return new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException is occurred.");

        return new ErrorResponse(
                ErrorCode.INVALIDED_REQUEST,
                ErrorCode.INVALIDED_REQUEST.getDescription());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse methodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException is occurred.");

        return new ErrorResponse(
                ErrorCode.INVALIDED_REQUEST,
                ErrorCode.INVALIDED_REQUEST.getDescription());
    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleAccountException(Exception e) {
        log.error("Exception is occurred", e);

        return new ErrorResponse(
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getDescription());
    }

}
