package api.server.common.exception.advice;


import api.server.common.exception.ErrorResponse;
import api.server.common.exception.custom.BusinessException;
import api.server.common.exception.custom.InvalidParameterException;
import api.server.common.exception.enums.ErrorCode;
import api.server.common.exception.enums.ErrorCodes;
import api.server.common.message.MessageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


/**
 * 정의되어 있는 Exception 클래스에 따라 handler 가 동작 됩니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    private final MessageHelper messageHelper;

    public GlobalExceptionAdvice(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    /**
     * 잘못된 파라미터 예외 발생 시
     */
    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<ErrorResponse> illegalArgumentExceptionHandler(IllegalArgumentException e) {

        log.error("IllegalArgumentException", e);
        ErrorCode errorCode = ErrorCode.INVALID_PARAMETER;

        return getErrorResponseEntity(e, errorCode);
    }


    /**
     * BusinessException 상속받은 클래스가 예외 발생 시
     */
    @ExceptionHandler(BusinessException.class)
    private ResponseEntity<ErrorResponse> businessExceptionHandler(BusinessException e) {

        log.error("BusinessException", e);
        ErrorCodes errorCode = e.getErrorCodes();
        return getErrorResponseEntity(e,  errorCode);
    }


    /**
     * &#064;Valid  검증 실패 예외 발생 시
     */
    @ExceptionHandler(InvalidParameterException.class)
    private ResponseEntity<ErrorResponse> invalidParameterExceptionHandler(InvalidParameterException e) {

        log.error("InvalidParameterException", e);
        ErrorCode errorCode = e.getErrorCode();

        ErrorResponse errorResponse = ErrorResponse
                .create()
                .message(messageHelper.getMessage(errorCode))
                .status(errorCode.getStatus())
                .code(errorCode.getCode())
                .errors(e.getErrors())
                .errorMessage(e.toString());
        return ResponseEntity.status(errorCode.getStatus()).body(errorResponse);
    }

    /**
     * <pre>
     * HTTP 표준 관련 예외
     * 잘못된 HTTP 메서드로 요청이 들어왔을 때 발생합니다.
     * </pre>
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.error("HttpRequestMethodNotSupportedException", e);

        ErrorResponse errorResponse = ErrorResponse.create()
                .message("HTTP 메서드가 지원되지 않습니다.")
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .code("METHOD_NOT_ALLOWED");

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }


    /**
     * <pre>
     * HTTP 표준 관련 예외
     * 클라이언트가 지원하지 않는 Content-Type을 사용했을 때 발생합니다.
     * </pre>
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.error("HttpMediaTypeNotSupportedException", e);

        ErrorResponse errorResponse = ErrorResponse.create()
                .message("지원하지 않는 Content-Type 입니다.")
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .code("UNSUPPORTED_MEDIA_TYPE");

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }


    /**
     * <pre>
     * 데이터 검증 및 유효성 관련 예외
     * &#064;Valid  또는 @Validated 검증 실패 시 발생.
     * </pre>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.error("ValidationException", e);

        ErrorResponse errorResponse = ErrorResponse.create()
                .message("요청 데이터가 유효하지 않습니다.")
                .status(HttpStatus.BAD_REQUEST.value())
                .code("VALIDATION_ERROR")
                .errors(e.getBindingResult());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * <pre>
     * 데이터 검증 및 유효성 관련 예외
     * &#064;Valid를  사용하지 않고 일반 객체에서 데이터 검증 실패 시 발생.
     * </pre>
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        log.error("ConstraintViolationException", e);

        List<String> errors = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.create()
                .message("제약 조건 위반 오류")
                .status(HttpStatus.BAD_REQUEST.value())
                .code("CONSTRAINT_VIOLATION")
                .errorMessage(String.join(", ", errors));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    /**
     * <pre>
     * 데이터 처리 관련 예외
     * 데이터 조회 시 결과가 없을 때 발생.
     * </pre>
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
        log.error("NoSuchElementException", e);

        ErrorResponse errorResponse = ErrorResponse.create()
                .message("요청한 데이터를 찾을 수 없습니다.")
                .status(HttpStatus.NOT_FOUND.value())
                .code("DATA_NOT_FOUND");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * <pre>
     * 데이터 처리 관련 예외
     * DB 제약 조건 위반(예: 중복 키, 외래 키 위반) 시 발생.
     * </pre>
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException", e);

        ErrorResponse errorResponse = ErrorResponse.create()
                .message("데이터 무결성 위반")
                .status(HttpStatus.CONFLICT.value())
                .code("DATA_INTEGRITY_VIOLATION");

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }


    /**
     * <pre>
     * 기타 예외 처리
     * 필수 요청 파라미터가 없을 때 발생.
     * </pre>
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException e) {
        log.error("MissingServletRequestParameterException", e);

        ErrorResponse errorResponse = ErrorResponse.create()
                .message("필수 요청 파라미터가 누락되었습니다.")
                .status(HttpStatus.BAD_REQUEST.value())
                .code("MISSING_PARAMETER");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    /*
      <pre>
      공통 예외 처리
      Exception 을 처리합니다.
      </pre>

    @Order(99)
    @ExceptionHandler(Exception.class)
    private ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("GenericException", e);
        return getErrorResponseEntity(e, ErrorCode.INTERNAL_SERVER_ERROR);
    }
     */

    /**
     * customException 을 처리합니다.
     */
    private ResponseEntity<ErrorResponse> getErrorResponseEntity(Exception e, ErrorCodes errorCodes) {
        ErrorResponse errorResponse = ErrorResponse
                .create()
                .message(messageHelper.getMessage(errorCodes))
                .status(errorCodes.getErrorCode().getStatus())
                .code(errorCodes.getErrorCode().getCode())
                .errorMessage(e.toString());
        return ResponseEntity.status(errorCodes.getErrorCode().getStatus()).body(errorResponse);
    }
}