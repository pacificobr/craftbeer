package com.beerhouse.error;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {NumberFormatException.class})
    protected ResponseEntity<Object> handleNumberFormatException(RuntimeException ex, WebRequest request) {
        ExceptionResponse bodyOfResponse = new ExceptionResponse(
                "The parameter sent is not valid [" + ex.getMessage().toLowerCase() + "].",
                HttpStatus.BAD_REQUEST);
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    class ExceptionResponse {
        private final String message;
        private final HttpStatus status;

        public ExceptionResponse(String message, HttpStatus status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }
}
