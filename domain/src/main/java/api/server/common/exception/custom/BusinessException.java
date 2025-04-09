package api.server.common.exception.custom;


import api.server.common.exception.enums.ErrorCodes;
import lombok.Getter;

import java.io.Serializable;

/**
 * <pre>
 * CustomException 상속받은 객체는
 * GlobalExceptionAdvice.customExceptionHandler 에서 공통적으로 처리됩니다.
 * </pre>
 */
@Getter
public class BusinessException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    private final transient ErrorCodes errorCodes; // 직렬화 제외를 위해 transient 추가
    private final transient Object[] args;

    public BusinessException(ErrorCodes errorCodes, Object... args) {
        this.errorCodes = errorCodes;
        this.args = args;
    }

}
