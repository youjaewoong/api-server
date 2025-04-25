package api.server.payment.infrastructure.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 가맹점 서비스 정보를 담는 DTO 클래스
*/
 @Data
public class MerchantServiceEntity {
    /** 가맹점 번호 */
    private Integer merchantNo;

    /** 가맹점 ID */
    private String merchantId;

    /** 상태 (A000: 정상, A001: 중지) */
    private String status;

    /** CVC 체크 여부 */
    private String cvvYn;

    /** 로그인 필요 여부 */
    private String loginYn;

    /** 검증 필요 여부 */
    private String verifyYn;

    /** 검증 한도 금액 */
    private BigDecimal verifyLimit;

    /** 중복 체크 여부 */
    private String dupChkYn;

    /** 가맹점 유형 (A201: 프리미엄) */
    private String type;

    /** 한도 금액 */
    private BigDecimal limitAmount;

    /** 비밀키 */
    private String secretKey;

    /** 한국 가맹점 ID */
    private String krMerchantId;

    /** 재시도 가맹점 ID */
    private String retryMerchantId;

    /** RMM 알림 여부 */
    private String rmmAlert;

    /** 로그인 화면 표시 여부 */
    private String loginViewYn;

    /** 보안 적용 여부 */
    private String secureYn;

    /** 3D 미적용 허용 여부 */
    private String non3dAllowYn;

    /** 응답 체크 여부 */
    private String chkResponse;

    /** 로고 이미지 경로 */
    private String logoImg;

    /** 결제 페이지 제목 */
    private String payTitle;

    /** DCC 사용 여부 */
    private String dccYn;

    /** MCP 사용 여부 */
    private String mcpYn;

    /** MCP 기준 통화 코드 */
    private String mcpFixedBaseCurrCode;
}
