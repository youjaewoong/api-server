package api.server.lottecard.service.enums;
import api.server.common.exception.enums.ErrorCodes;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * <pre>
 * ErrorCode 를 정의 합니다
 *  - status : http status code
 *  - code : custom code
 *  - message : table cm_mlangmessage  에 정의된 MSG_KEY
 *  - defaultMessage : cm_mlangmessage table error 발생 시 메시지 값
 * </pre>
 */
@Getter
@AllArgsConstructor
public enum LottecardErrorCode implements ErrorCodes {

    // 5XX 에러
    INTERNAL_SERVER_ERROR(500, "API01", "rest.api.error.01",
            "서버 내부에 오류가 발생했습니다."),

    BUSINESS_SERVICE_ERROR(500, "API02", "rest.api.error.02",
                                  "서버 내부 로직 처리 중 오류가 발생했습니다.")

            ;

    private final int status;
    private final String code;
    private final String message;
    private final String defaultMessage;

    @Override
    public CommonErrorCode getErrorCode() {
        return CommonErrorCode.builder()
                .status(this.status)
                .code(this.code)
                .message(this.message)
                .defaultMessage(this.defaultMessage)
                .build();
    }
}