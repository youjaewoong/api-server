package api.server.payment.errors;

import api.server.exception.enums.ErrorCodes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


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
public enum PaymentErrorCode implements ErrorCodes {

    // 결제 관련 에러
    PAYMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND.value(),
            "P001",
            "결제 정보를 찾을 수 없습니다. (결제 ID: {0})",
            "존재하지 않는 결제입니다."
    ),
    INVALID_PAYMENT_STATUS(
            HttpStatus.BAD_REQUEST.value(),
            "P002",
            "잘못된 결제 상태입니다. (현재 상태: {0}, 요청 상태: {1})",
            "잘못된 결제 상태입니다."
    ),
    INVALID_PAYMENT_AMOUNT(
            HttpStatus.BAD_REQUEST.value(),
            "P003",
            "잘못된 결제 금액입니다. (요청 금액: {0})",
            "잘못된 결제 금액입니다."
    ),

    // 가맹점 관련 에러
    MERCHANT_NOT_FOUND(
            HttpStatus.NOT_FOUND.value(),
            "M001",
            "가맹점 정보를 찾을 수 없습니다. (가맹점 ID: {0})",
            "존재하지 않는 가맹점입니다."
    ),
    MERCHANT_NOT_ACTIVE(
            HttpStatus.BAD_REQUEST.value(),
            "M002",
            "비활성화된 가맹점입니다. (가맹점 ID: {0}, 상태: {1})",
            "비활성화된 가맹점입니다."
    ),

    // 카드 관련 에러
    INVALID_CARD_NUMBER(
            HttpStatus.BAD_REQUEST.value(),
            "C001",
            "잘못된 카드 번호입니다. (카드번호: {0})",
            "잘못된 카드 번호입니다."
    ),
    EXPIRED_CARD(
            HttpStatus.BAD_REQUEST.value(),
            "C002",
            "만료된 카드입니다. (만료일: {0})",
            "만료된 카드입니다."
    ),

    // System Error Codes (SYxx)
    SYSTEM_DB_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "SY03",
            "[{0}] 데이터베이스 오류가 발생했습니다.",
            "데이터베이스 오류가 발생했습니다."
    ),
    SYSTEM_VAN_CONNECTION_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "SY20",
            "[{0}] VAN 연결 중 오류가 발생했습니다.",
            "VAN 연결 중 오류가 발생했습니다."
    ),
    SYSTEM_VAN_DATA_CREATE_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "SY21",
            "[{0}] VAN 데이터 생성 중 오류가 발생했습니다.",
            "VAN 데이터 생성 중 오류가 발생했습니다."
    ),

    // Business Error Codes (Sxxx)
    MERCHANT_NOT_REGISTERED(
            HttpStatus.BAD_REQUEST.value(),
            "S010",
            "[{0}] 미등록된 가맹점입니다.",
            "미등록된 가맹점입니다."
    ),
    MERCHANT_SUSPENDED(
            HttpStatus.BAD_REQUEST.value(),
            "S011",
            "[{0}] 정지 또는 해지된 가맹점입니다.",
            "정지 또는 해지된 가맹점입니다."
    ),
    CURRENCY_MISMATCH(
            HttpStatus.BAD_REQUEST.value(),
            "S012",
            "[{0}] 승인통화가 일치하지 않습니다.",
            "승인통화가 일치하지 않습니다."
    ),
    ACQUIRER_MERCHANT_NOT_REGISTERED(
            HttpStatus.BAD_REQUEST.value(),
            "S013",
            "[{0}] 매입사 미등록 가맹점입니다. (매입사 가맹점번호: {1}, 서브몰 사업자번호: {2})",
            "매입사 미등록 가맹점입니다."
    );

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