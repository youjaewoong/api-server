package api.server.gateway.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 거래 웹 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantServiceEntity {
    private String name;            // 가맹점명
    private String service;         // 서비스 코드
    private String url;            // URL
    private String merchantNo;      // 가맹점 번호
    private String sStatus;        // 서비스 상태
    private String cvvYn;          // CVV 체크 여부
    private String loginYn;        // 로그인 필요 여부
    private String verifyYn;       // 검증 필요 여부
    private int verifyLimit;       // 검증 한도
    private String dupChkYn;       // 중복 체크 여부
    private String mStatus;        // 가맹점 상태
    private String type;           // 가맹점 타입
    private long limitAmount;      // 한도 금액
    private String secretKey;      // 비밀키
    private String krMerchantId;   // 국내 가맹점 ID
    private String retryMerchantId;// 재시도 가맹점 ID
    private String rmmAlert;       // RMM 알림 여부
    private String loginViewYn;    // 로그인 화면 표시 여부
    private String secureYn;       // 보안 적용 여부
}
