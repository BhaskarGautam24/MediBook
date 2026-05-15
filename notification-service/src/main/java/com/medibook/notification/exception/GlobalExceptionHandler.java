package com.medibook.notification.exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice @Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class) public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) { return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage())); }
    @ExceptionHandler(Exception.class) public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) { log.error("Error: ", ex); return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unexpected error")); }
}
