package api.server.payment.infrastructure.entity;

import lombok.Data;


/**
 * 거래 정보를 담는 DTO 클래스
 */
@Data
public class ServiceCurrencyEntity {
    private String merchantid;      // 상점 아이디
    private String cardcode;        // 카드 코드
    private String reqcurrency;     // 요청 통화코드
    private String apprvcurrency;   // 승인 통화코드
    private String payto;           // 정산 대상 금액
    private String minamount;       // 최소 결제 금액
    private Integer vanno;          // VAN 번호

}
