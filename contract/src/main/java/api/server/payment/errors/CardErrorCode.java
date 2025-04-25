package api.server.payment.errors;
import api.server.exception.enums.ErrorCodes;
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
public enum CardErrorCode implements ErrorCodes {

    // 카드번호 관련 에러 (400: Bad Request)
    INVALID_NUMBER(400, "CN01", "유효하지 않은 카드번호입니다.", "Invalid card number format"),
    INVALID_LENGTH(400, "CN02", "카드번호 길이가 올바르지 않습니다.", "Card number length must be between 15 and 16"),
    MASKING_ERROR(400, "CN03", "카드번호 마스킹 처리 중 오류가 발생했습니다.", "Error in card number masking process"),

    // 카드 검증 관련 에러 (401: Unauthorized)
    INVALID_CVC(401, "CV01", "CVC 번호가 올바르지 않습니다.", "Invalid CVC number"),
    EXPIRED_CARD(401, "CV02", "만료된 카드입니다.", "Card has expired"),
    INVALID_EXPIRY(401, "CV03", "유효기간이 올바르지 않습니다.", "Invalid expiry date"),

    // 카드 거래 제한 에러 (403: Forbidden)
    CARD_BLOCKED(403, "CT01", "거래가 제한된 카드입니다.", "Card is blocked for transactions"),
    EXCEED_LIMIT(403, "CT02", "한도초과 거래입니다.", "Transaction exceeds card limit"),
    INVALID_INSTALLMENT(403, "CT03", "유효하지 않은 할부 개월 수입니다.", "Invalid installment months"),

    // 필수 데이터 검증 에러 (W1xx)
    INVALID_CURRENCY(400, "W100", "유효하지 않은 통화 코드입니다.", "Invalid currency code"),
    INVALID_AMOUNT(400, "W101", "유효하지 않은 금액입니다.", "Invalid amount"),
    INVALID_MERCHANT_REF(400, "W102", "유효하지 않은 가맹점 참조번호입니다.", "Invalid merchant reference"),
    INVALID_BUYER(400, "W110", "구매자 정보가 유효하지 않습니다.", "Invalid buyer information"),
    
    // 카드 데이터 검증 에러 (W12x)
    INVALID_CARD_TYPE(400, "W121", "유효하지 않은 카드 타입입니다.", "Invalid card type"),
    INVALID_CARD_NUMBER(400, "W122", "유효하지 않은 카드번호입니다.", "Invalid card number"),
    INVALID_EXPIRY_DATE(400, "W123", "유효하지 않은 유효기간입니다.", "Invalid expiry date"),
    INVALID_MERCHANT_ID(400, "W129", "유효하지 않은 가맹점 ID입니다.", "Invalid merchant ID"),
    
    // 승인 제한 에러 (W2xx)
    BELOW_MINIMUM_AMOUNT(403, "W203", "최소 결제금액 미만입니다.", "Amount below minimum"),
    CURRENCY_MISMATCH(403, "W204", "승인통화가 일치하지 않습니다.", "Currency mismatch"),
    
    // 시스템 에러 (Sxxx)
    CARD_ISSUED_IN_KOREA(403, "S021", "국내발급 카드는 사용할 수 없습니다.", "Card issued in Korea is not allowed");


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