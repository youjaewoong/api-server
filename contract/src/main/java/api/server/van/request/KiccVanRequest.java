package api.server.van.request;

import api.server.common.annotation.FixedLength;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "KICC VAN 요청")
public class KiccVanRequest {

    @Schema(description = "길이를 제외한 전문 총 길이", example = "0200")
    @FixedLength(length = 4, offset = 0)
    private String totalLength;

    @Schema(description = "전문암호여부: 2=암호화, else=0", example = "2")
    @FixedLength(length = 1, offset = 4)
    private String encryptionFlag;

    @Schema(description = "전문변경일자 (YYMM)", example = "0412")
    @FixedLength(length = 4, offset = 5)
    private String changeDate;

    @Schema(description = "단말기번호", example = "1234567890")
    @FixedLength(length = 10, offset = 9)
    private String terminalId;

    @Schema(description = "체크카드번호", example = "12345")
    @FixedLength(length = 5, offset = 19)
    private String checkCardNumber;

    @Schema(description = "전문일련번호", example = "000000000000")
    @FixedLength(length = 12, offset = 24)
    private String serialNumber;

    @Schema(description = "가맹점 Timeout (Default: 15)", example = "15")
    @FixedLength(length = 2, offset = 36)
    private String timeout;

    @Schema(description = "프로그램 관리자명", example = "홍길동")
    @FixedLength(length = 20, offset = 38)
    private String managerName;

    @Schema(description = "회사전용단말번호", example = "XXXXXXXXXXXXX")
    @FixedLength(length = 13, offset = 58)
    private String companyTerminalNumber;

    @Schema(description = "확대단말기번호", example = "YYYYYYYYYYYYY")
    @FixedLength(length = 13, offset = 71)
    private String extendedTerminalNumber;

    @Schema(description = "여유필드", example = "")
    @FixedLength(length = 43, offset = 84)
    private String reservedField;

    @Schema(description = "전문구분", example = "1")
    @FixedLength(length = 1, offset = 127)
    private String messageType;

    @Schema(description = "POS Entry Mode", example = "1")
    @FixedLength(length = 1, offset = 131)
    private String posEntryMode;

    @Schema(description = "카드번호 또는 VAN-TR", example = "12345678901234567890")
    @FixedLength(length = 37, offset = 132)
    private String cardNumber;

    @Schema(description = "할부개월수", example = "00")
    @FixedLength(length = 2, offset = 169)
    private String installment;

    @Schema(description = "통화구분", example = "1")
    @FixedLength(length = 1, offset = 171)
    private String currencyType;

    @Schema(description = "환산소수점자릿수", example = "0")
    @FixedLength(length = 1, offset = 172)
    private String decimalPoint;

    @Schema(description = "공급금액", example = "000000000000")
    @FixedLength(length = 12, offset = 173)
    private String supplyAmount;

    @Schema(description = "봉사료", example = "000000000000")
    @FixedLength(length = 12, offset = 185)
    private String serviceCharge;

    @Schema(description = "세금", example = "000000000000")
    @FixedLength(length = 12, offset = 197)
    private String tax;

    @Schema(description = "승인번호", example = "123456")
    @FixedLength(length = 12, offset = 209)
    private String approvalNumber;

    @Schema(description = "거래일자", example = "240401")
    @FixedLength(length = 6, offset = 221)
    private String transactionDate;

    @Schema(description = "Working Key Index", example = "00")
    @FixedLength(length = 2, offset = 227)
    private String workingKeyIndex;

    @Schema(description = "비밀번호", example = "0000000000000000")
    @FixedLength(length = 16, offset = 229)
    private String password;

    @Schema(description = "상품코드", example = "ABC123")
    @FixedLength(length = 6, offset = 245)
    private String productCode;

    @Schema(description = "주민등록 또는 사업자번호", example = "8801011234567")
    @FixedLength(length = 10, offset = 251)
    private String idOrBusinessNumber;

    @Schema(description = "전자상거래 구분", example = "1")
    @FixedLength(length = 1, offset = 261)
    private String commerceFlag;

    @Schema(description = "도메인", example = "example.com")
    @FixedLength(length = 40, offset = 262)
    private String domain;

    @Schema(description = "서버 IP", example = "192.168.0.1")
    @FixedLength(length = 20, offset = 302)
    private String serverIp;

    @Schema(description = "물사업자번호", example = "0000000000")
    @FixedLength(length = 10, offset = 322)
    private String merchantBusinessNumber;

    @Schema(description = "카드정렬순구분", example = "1")
    @FixedLength(length = 1, offset = 332)
    private String cardSortCode;

    @Schema(description = "가맹점사 ID", example = "AA")
    @FixedLength(length = 2, offset = 333)
    private String merchantCompanyId;

    @Schema(description = "가맹점사용필드", example = "CUSTOMDATA000000000000000000000")
    @FixedLength(length = 30, offset = 335)
    private String merchantCustomField;

    // 수표번호
    @Schema(description = "수표번호", example = "12345678")
    @FixedLength(length = 8, offset = 365)
    private String checkNumber;

    // 수표발행은행
    @Schema(description = "수표발행은행", example = "11")
    @FixedLength(length = 2, offset = 373)
    private String checkBankCode;

    // 수표발행영업점
    @Schema(description = "수표발행영업점", example = "1234")
    @FixedLength(length = 4, offset = 375)
    private String checkBranchCode;

    // 권종코드
    @Schema(description = "권종코드", example = "13")  // 13: 10만원, 14: 30만원, 15: 50만원 등
    @FixedLength(length = 2, offset = 379)
    private String bondTypeCode;

    // 수표금액
    @Schema(description = "수표금액", example = "000000012345")
    @FixedLength(length = 12, offset = 381)
    private String checkAmount;

    // 수표발행일
    @Schema(description = "수표발행일", example = "240401")
    @FixedLength(length = 6, offset = 393)
    private String checkIssuedDate;

    // 계좌입력번호
    @Schema(description = "계좌입력번호", example = "123456")
    @FixedLength(length = 6, offset = 399)
    private String accountInputNumber;

    // 예비1 - cvv2
    @Schema(description = "CVV2 코드", example = "123")
    @FixedLength(length = 3, offset = 405)
    private String cvv2;

    // 예비2
    @Schema(description = "예비 필드 2", example = "")
    @FixedLength(length = 27, offset = 408)
    private String reserved2;

    // 공인인증구분
    @Schema(description = "공인인증 구분", example = "I") // 예: I, E, R, V, 공백 등
    @FixedLength(length = 1, offset = 435)
    private String digitalCertType;

    // MPI Module
    @Schema(description = "MPI Module", example = "K") // 예: K, R, *, 공백 등
    @FixedLength(length = 1, offset = 436)
    private String mpiModule;

    // Cavv 재사용
    @Schema(description = "Cavv 재사용 여부", example = "Y") // 예: Y, N
    @FixedLength(length = 1, offset = 437)
    private String cavvReuse;

    // 공인인증 Data
    @Schema(description = "공인인증 전체 데이터 블록", example = "공인인증전자서명블록...")
    @FixedLength(length = 100, offset = 438)
    private String digitalCertData;
}
