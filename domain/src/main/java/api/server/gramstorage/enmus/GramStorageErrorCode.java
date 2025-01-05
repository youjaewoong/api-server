package api.server.gramstorage.enmus;
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
public enum GramStorageErrorCode implements ErrorCodes {

    // 4XX 에러
    DATA_NOT_FOUND(404, "GRAM01", "gram.storage.01",
            "{0} 데이터가 존재하지 않습니다."),

    INVALID_REQUEST(400, "GRAM02", "gram.storage.02",
            "잘못된 요청입니다."),

    // 5XX 에러
    INTERNAL_SERVER_ERROR(500, "GRAM03", "gram.storage.03",
            "내부 서버 오류가 발생했습니다."),

    FILE_STORAGE_FAILED(500, "GRAM04", "gram.storage.04",
            "파일 저장에 실패했습니다.");

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