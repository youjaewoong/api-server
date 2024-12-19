package api.server.common.exception.custom;


import api.server.common.exception.enums.ErrorCodes;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * <pre>
 * CustomException 상속받은 객체는
 * GlobalExceptionAdvice.customExceptionHandler 에서 공통적으로 처리됩니다.
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCodes errorCodes;

    public BusinessException(ErrorCodes errorCodes) {
        this.errorCodes = errorCodes;
    }

}
