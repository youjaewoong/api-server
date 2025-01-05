package api.server.common.exception.enums;
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
public enum ErrorCode implements ErrorCodes {

    // 4XX
    DATA_NOT_FOUND(404, "C001", "error.data.not.found", "데이터가 존재하지않습니다."),
    USER_NOT_FOUND(404, "C002", "error.user.not.found", "유저정보가 존재하지 않습니다."),

    DELETE_FAIL(400, "C003", "error.delete.fail", "삭제에 실패했습니다."),
    INSERT_FAIL(400, "C004", "error.insert.fail", "저장에 실패했습니다."),
    UPDATE_FAIL(400, "C005", "error.update.fail", "수정에 실패했습니다."),

    INVALID_PARAMETER(400, "C006", "error.invalid.parameter {0}", "요청 데이터가 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(405, "C007", "error.method.not.allowed", "메소드 타입의 접근이 올바르지 않습니다."),

    // 500
    INTERNAL_SERVER_ERROR(500, "C008", "error.internal.server.error", "서버에 내부 오류가 발생했습니다.");

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