package api.server.gateway.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionWebEntity {
    private String merchantId;     // 가맹점 ID
    private String ref;           // 참조번호
    private String currency;      // 통화
    private Long amount;          // 금액
    private String email;         // 이메일
    private String buyer;         // 구매자
    private String tel;           // 전화번호
    private String accessIp;      // 접속 IP
    private String country;       // 국가
    private String accessCountry; // 접속 국가
    private Integer webNo;        // 웹 거래 번호
}
