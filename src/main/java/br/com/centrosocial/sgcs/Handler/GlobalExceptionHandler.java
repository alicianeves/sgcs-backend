package br.com.centrosocial.sgcs.Handler;

import br.com.centrosocial.sgcs.Exception.BusinessException;
import br.com.centrosocial.sgcs.Exception.ConflictException;
import br.com.centrosocial.sgcs.Exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException exception) {
        return createResponse(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        return createResponse(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException exception) {
        return createResponse(HttpStatus.CONFLICT, "Já existe um registro com esses dados.");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException exception) {
        return createResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException exception) {
        return createResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessException exception) {
        return createResponse(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return createResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Dados inválidos.");

        return createResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {
        LOGGER.error("Erro inesperado ao processar requisição", exception);
        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno.");
    }

    private ResponseEntity<ErrorResponse> createResponse(HttpStatus status, String message) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );

        return ResponseEntity.status(status).body(error);
    }

    private record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message
    ) {
    }
}
