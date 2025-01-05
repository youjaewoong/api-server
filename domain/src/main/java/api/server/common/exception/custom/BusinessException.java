package api.server.common.exception.custom;


import api.server.common.exception.enums.ErrorCodes;
import lombok.Getter;

/**
 * <pre>
 * CustomException 상속받은 객체는
 * GlobalExceptionAdvice.customExceptionHandler 에서 공통적으로 처리됩니다.
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCodes errorCodes;

    public BusinessException(ErrorCodes errorCodes, Object... args) {
        super(errorCodes.getErrorCode().formatMessage(args)); // 포맷된 메시지를 사용
        this.errorCodes = errorCodes;
    }
}
