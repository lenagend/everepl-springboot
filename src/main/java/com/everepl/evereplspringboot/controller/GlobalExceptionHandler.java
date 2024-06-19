package com.everepl.evereplspringboot.controller;

import com.everepl.evereplspringboot.exceptions.AlreadyExistsException;
import com.everepl.evereplspringboot.exceptions.InvalidUrlException;
import com.everepl.evereplspringboot.exceptions.UserActionRestrictionException;
import com.everepl.evereplspringboot.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final EmailService emailService;

    public GlobalExceptionHandler(EmailService emailService) {
        this.emailService = emailService;
    }

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNoResourceFoundException(NoResourceFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        return response;
    }

    @ExceptionHandler(UserActionRestrictionException.class)
    public ResponseEntity<Object> handleUserActionRestrictionException(UserActionRestrictionException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<Object> handleAlreadyExistsException(AlreadyExistsException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        //특정오류들을 런타임에러로 매핑했을경우 여기서 분기처리를한다.
        InvalidUrlException invalidUrlException = (InvalidUrlException) ex.getCause();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", invalidUrlException.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<Object> handleMessagingException(MessagingException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST);
        body.put("error", "Messaging error");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 벨리데이션 관련 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body("입력값 검증에 실패했습니다: " + errors);
    }

    //noSuch 예외처리
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Object> handleNoSuchElementException(NoSuchElementException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "요청한 항목을 찾을 수 없습니다.");

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    //UsernameNotFoundException 예외처리
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "유저정보를 불러올 수 없습니다. 로그인 상태를 확인해주세요.");

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    //IllegalStateException 예외처리
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    //데이터베이스 관련 예외처리
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDatabaseException(DataAccessException ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Error ID: {}, Request URL: {}, Database operation exception occurred - Exception: {}, Message: {}", errorId, request.getDescription(false), ex.getClass().getName(), ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("errorId", errorId);
        body.put("message", "데이터베이스 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //입출력 에러 관련 예외처리
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Error ID: {}, Request URL: {}, I/O exception occurred - Exception: {}, Message: {}", errorId, request.getDescription(false), ex.getClass().getName(), ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("errorId", errorId);
        body.put("message", "파일 입출력 중 오류가 발생했습니다.");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //메모리 부족에 대한 처리
    @ExceptionHandler(OutOfMemoryError.class)
    public ResponseEntity<Object> handleOutOfMemoryError(OutOfMemoryError error, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Error ID: {}, Request URL: {}, System out of memory - Error: {}", errorId, request.getDescription(false), error.getMessage());

        emailService.sendErrorEmail("시스템 메모리 부족", "시스템에 메모리가 부족합니다. 즉각적인 조치가 필요합니다.");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("errorId", errorId);
        body.put("message", "시스템 오류가 발생했습니다. 관리자에게 문의해주세요.");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // 처리되지 않은 모든 예외에 대한 핸들러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtException(Exception ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        String requestDetails = request.getDescription(false);

        log.error("Error ID: {}, Request URL: {}, An unexpected error occurred - Exception: {}, Message: {}, Stack Trace: {}",
                errorId, requestDetails, ex.getClass().getName(), ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("errorId", errorId);
        body.put("message", "알 수 없는 오류가 발생했습니다. 지원팀에 문의해주세요.");
        body.put("details", requestDetails);  // 요청의 세부 정보
        body.put("exception", ex.getClass().getName());  // 예외의 유형

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
