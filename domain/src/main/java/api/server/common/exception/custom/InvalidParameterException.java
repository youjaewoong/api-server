package api.server.common.exception.custom;

import org.springframework.validation.Errors;

import api.server.common.exception.enums.ErrorCode;
import lombok.Getter;

/**
 * <pre>
 * 요청 객체의 validation 검증 실패 시 사용됩니다.
 * 메시지
 *   - code : error.internal.server.error
 *   - ko : 서버에 내부 오류가 발생했습니다.
 *   - en : The server encountered an internal error.
 * </pre>
 */
@Getter
public class InvalidParameterException extends RuntimeException {

	private static final long serialVersionUID = -5368895316342160354L;
	private final Errors errors;
    private final ErrorCode errorCode;

    public InvalidParameterException(Errors errors) {
        this.errorCode = ErrorCode.INVALID_PARAMETER;
        this.errors = errors;
    }

}