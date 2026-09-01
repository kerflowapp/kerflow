package com.kerflowapp.kerflow.api;

import com.kerflowapp.kerflow.exceptions.KerflowException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler(KerflowException.class)
    protected ResponseEntity<ApiError> handle(KerflowException e, HttpServletRequest request) {
        LOGGER.info("received error '{}' from ws", e.getMessage(), e);
        return new ApiError(e.getCode().name(), e.getMessage(), HttpStatus.BAD_REQUEST, request).toResponseEntity();
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ApiError> handle(AccessDeniedException e, HttpServletRequest request) {
        LOGGER.info("received error '{}' from ws", e.getMessage(), e);
        return new ApiError(HttpStatus.FORBIDDEN.getReasonPhrase(), e.getMessage(),
            HttpStatus.FORBIDDEN, request).toResponseEntity();
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ApiError> handle(RuntimeException e, HttpServletRequest request) {
        LOGGER.info("received error '{}' from ws", e.getMessage(), e);
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Something went wrong",
            HttpStatus.INTERNAL_SERVER_ERROR, request).toResponseEntity();
    }

}
