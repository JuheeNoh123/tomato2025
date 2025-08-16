package com.sku_likelion.Moving_Cash_back.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidUserException.class)
    public ResponseEntity<String> invalidUser(InvalidUserException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<String> invalidPassword(InvalidPasswordException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(InvalidIdException.class)
    public ResponseEntity<String> invalidId(InvalidUserException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }


    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<String> duplicateUser(DuplicateUserException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(HandleJwtException.class)
    public ResponseEntity<String> handleJwt(HandleJwtException e){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String dbMessage = e.getMostSpecificCause().getMessage();

        // 제약 조건 이름에 따라 메시지 매핑
        if (dbMessage.contains("uk_user_challenge")) {
            return ResponseEntity.badRequest().body("이미 참여한 챌린지입니다.");
        } else if (dbMessage.contains("uk_session_point")) {
            return ResponseEntity.badRequest().body("이미 해당 포인트가 존재합니다.");
        }

        // 기본 메시지 (그 외 제약 조건 위반)
        return ResponseEntity.badRequest().body("데이터 제약 조건 위반 발생");
    }

    @ExceptionHandler(InvalidChallengeException.class)
    public ResponseEntity<String> invalidChallenge(InvalidChallengeException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

}
