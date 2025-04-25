package api.server.payment.infrastructure.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionEntity {

    /** 거래 번호 */
    private Integer transNo;

    /** 가맹점 ID */
    private String merchantId;

    /** 거래 ID */
    private String transId;

    /** 가맹점 번호 */
    private Integer merchantNo;

    /** VAN사 번호 */
    private Integer vanNo;

    /** VAN사 상점 ID */
    private String vanShopId;

    /** 거래 유형 (T000: 일반승인, T002: 취소, T003: 인증, T004: 매입, T005: 부분취소, T006: 인증취소, T007: 매입완료) */
    private String transType;

    /** 총 거래 금액 */
    private BigDecimal totalAmount;

    /** 카드 유효기간 (암호화) */
    private byte[] expiryDate;

    /** 서비스 유형 */
    private String serviceType;

    /** 거래 통화 */
    private String currency;

    /** 환율 번호 */
    private Integer exgNo;

    /** 카드 코드 */
    private String cardCode;

    /** 보안 거래 ID */
    private String secureTransId;

    /** 승인 통화 */
    private String apprvCurrency;

    /** 승인 금액 */
    private BigDecimal apprvAmount;

    /** 암호화된 카드 번호 */
    private byte[] encPan;

    /** 암호화된 CVC */
    private byte[] encCvv;

    /** 카드번호 앞 4자리 */
    private String cardNo1;

    /** 카드번호 뒤 4자리 */
    private String cardNo4;

    /** VAN사 통화 */
    private String vanCurrency;

    /** VAN사 금액 */
    private BigDecimal vanAmount;

    /** VAN사 환율 번호 */
    private Integer vanExgNo;

    /** 암호화 키 인덱스 */
    private String keyIndex;

    /** 요청 일시 */
    private LocalDateTime reqDt;

    /** 응답 일시 */
    private LocalDateTime resDt;

    /** 거래 상태 (S100: 정상, S101: 취소요청, S102: 취소완료) */
    private String status;
}
