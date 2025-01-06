package api.server.common.exception.custom;


import api.server.common.exception.enums.ErrorCodes;
import api.server.common.message.MessageHelper;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

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

    // MessageHelper를 정적으로 설정
    @Setter
    private static MessageHelper messageHelper;

    public BusinessException(ErrorCodes errorCodes, Object... args) {
        this.errorCodes = errorCodes;
        this.args = args;
    }

}
