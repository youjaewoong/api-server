package api.server.common.message;

import api.server.common.exception.enums.ErrorCodes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;

@Slf4j
@Component
public class MessageHelper {

    private final MessageSource messageSource;

    public MessageHelper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    /**
     * <pre>
     *   ErrorCode 에 정의되어 있는 메시지값을 호출 합니다.
     *   에러 발생 시 defaultMessage 로 대체합니다.
     * </pre>
     */
    public String getMessage(ErrorCodes errorCodes, Object... args) {
        try {
            return messageSource.getMessage(
                    errorCodes.getErrorCode().getMessage(),
                    args.length > 0 ? args : null, // args 배열 확인 후 전달
                    LocaleContextHolder.getLocale()
            );
        } catch (Exception e) {
            log.error("Failed to retrieve message for code: {}", errorCodes.getErrorCode().getMessage(), e);
            return errorCodes.getErrorCode().getDefaultMessage();
        }
    }

}
