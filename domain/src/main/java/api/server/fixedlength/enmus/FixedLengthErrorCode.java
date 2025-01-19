package api.server.fixedlength.enmus;
import api.server.common.exception.enums.ErrorCodes;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * <pre>
 * FixedLengthErrorCode
 * ErrorCode 를 정의합니다.
 *  - status : HTTP 상태 코드
 *  - code : 커스텀 코드
 *  - message : 메시지 키 (다국어 지원 가능)
 *  - defaultMessage : 메시지 키를 찾지 못했을 때의 기본 메시지
 * </pre>
 */
@Getter
@AllArgsConstructor
public enum FixedLengthErrorCode implements ErrorCodes {

    // 4XX
    DATA_NOT_FOUND(404, "FIXED10", "fixed.length.error.10", "데이터가 존재하지않습니다."),
    USER_NOT_FOUND(404, "FIXED11", "fixed.length.error.11", "유저정보가 존재하지 않습니다."),


    DELETE_FAIL(400, "FIXED20", "fixed.length.error.20", "삭제에 실패했습니다."),
    INSERT_FAIL(400, "FIXED21", "fixed.length.error.21", "저장에 실패했습니다."),
    UPDATE_FAIL(400, "FIXED22", "fixed.length.error.22", "수정에 실패했습니다."),
    SOCKET_FAIL(400, "FIXED23", "fixed.length.error.23", "소켓통신에 실패했습니다."),

    INVALID_PARAMETER(400, "FIXED30", "fixed.length.error.30", "요청 데이터가 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(405, "FIXED31", "fixed.length.error.31", "메소드 타입의 접근이 올바르지 않습니다."),

    // 500
    INTERNAL_SERVER_ERROR(500, "FIXED40", "fixed.length.error.40", "서버에 내부 오류가 발생했습니다."),
    JSON_LOAN_ERROR(500, "FIXED41", "fixed.length.error.41", "JSON 파일로드를 실패했습니다. {0}");

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