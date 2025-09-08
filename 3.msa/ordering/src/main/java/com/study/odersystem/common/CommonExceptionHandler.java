package com.study.odersystem.common;

import com.study.odersystem.common.dto.ResponseDto;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(value = EntityExistsException.class)
    public ResponseEntity<?> handleEntityExistsException(EntityExistsException e) {
        return ResponseEntity.badRequest().body(
                ResponseDto.ofFailure(
                        HttpStatus.BAD_REQUEST.value(),
                        e.getMessage()
                )
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ResponseDto.ofFailure(
                        HttpStatus.FORBIDDEN.value(),
                        e.getMessage()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.badRequest().body(ResponseDto.ofFailure(HttpStatus.BAD_REQUEST.value(), errorMsg));
        //(HttpStatus.BAD_REQUEST.value(), errorMsg), HttpStatus.BAD_REQUEST
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ResponseDto.ofFailure(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> exception(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseDto.ofFailure(HttpStatus.CONFLICT.value(), e.getMessage()));
    }


}
