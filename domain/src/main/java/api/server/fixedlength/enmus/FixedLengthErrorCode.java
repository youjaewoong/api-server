package api.server.fixedlength.enmus;
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
public enum FixedLengthErrorCode implements ErrorCodes {

    // 4XX
    DATA_NOT_FOUND(404, "FIXED01", "fixed.length.error.01", "데이터가 존재하지않습니다."),
    USER_NOT_FOUND(404, "FIXED02", "fixed.length.error.02", "유저정보가 존재하지 않습니다."),

    DELETE_FAIL(400, "FIXED03", "fixed.length.error.03", "삭제에 실패했습니다."),
    INSERT_FAIL(400, "FIXED04", "fixed.length.error.04", "저장에 실패했습니다."),
    UPDATE_FAIL(400, "FIXED05", "fixed.length.error.05", "수정에 실패했습니다."),

    INVALID_PARAMETER(400, "FIXED06", "fixed.length.error.06", "요청 데이터가 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(405, "FIXED07", "fixed.length.error.07", "메소드 타입의 접근이 올바르지 않습니다."),

    // 500
    INTERNAL_SERVER_ERROR(500, "FIXED08", "fixed.length.error.08", "서버에 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
    private final String defaultMessage;

    @Override
    public CommonErrorCode getErrorCode() {
        return CommonErrorCode.builder()
                .status(this.status)
                .code(this.code)
                .message(this.defaultMessage)
                .defaultMessage(this.getDefaultMessage())
                .build();
    }
}