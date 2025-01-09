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

    private final ErrorCodes errorCodes; // 에러 코드
    private final Object[] args; // 에러 코드

    public BusinessException(ErrorCodes errorCodes, Object... args) {
        this.errorCodes = errorCodes;
        this.args = args;
    }

}
