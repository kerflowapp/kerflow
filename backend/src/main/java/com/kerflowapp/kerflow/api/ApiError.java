package com.kerflowapp.kerflow.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    public final long timestamp;
    public final String error;
    public final String path;
    public final String errorCode;
    public final List<String> detailedMessages;
    @JsonIgnore
    private final HttpStatus httpStatus;

    public ApiError(String code, String message, HttpStatus status, HttpServletRequest request) {
        this.timestamp = System.currentTimeMillis();
        this.httpStatus = status;
        this.error = message == null ? status.getReasonPhrase() : message;
        this.path = request.getServletPath();

        this.detailedMessages = null;
        this.errorCode = code;
    }

    public int getStatus() {
        return httpStatus.value();
    }

    public ResponseEntity<ApiError> toResponseEntity() {
        return this.toResponseEntity(new HttpHeaders());
    }

    public ResponseEntity<ApiError> toResponseEntity(HttpHeaders headers) {
        return new ResponseEntity<>(this, headers, httpStatus);
    }
}
