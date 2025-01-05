package api.server.common.exception.enums;

import lombok.Builder;
import lombok.Getter;

import java.text.MessageFormat;

/**
 * 업무별 처리를 위한 인터페이스 클래스
 */
public interface ErrorCodes {

    CommonErrorCode getErrorCode();

    /**
     * 업무별 바인딩 되는 공통 객체
     */
    @Getter
    @Builder
    class CommonErrorCode {
        private int status;
        private final String code;
        private final String message;
        private final String defaultMessage;

        /**
         * 메시지 포맷팅 기능 추가
         */
        public String formatMessage(Object... args) {
            return MessageFormat.format(this.message, args);
        }
    }

}