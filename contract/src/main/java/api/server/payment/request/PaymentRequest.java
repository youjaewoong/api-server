package api.server.payment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *   <pre>
 *    F0100:     //신용카드 승인
 *    F0120:     //신용카드 인증(수동매입)
 *    F0101:     //신용카드 승인(PCINS)
 *    F0121:     //신용카드 인증(PCINS)
 *    F0102:     //BC UPOT 신용카드 승인(자체 저장)
 *    F0122:     //BC UPOT 신용카드 승인(자체 저장 수동매입)
 *    F0400:     //일반 환급요청
 *    F9999:     //Edgar 부하테스트 (2023.03.15)
 *   </pre>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "결제 요청")
public class PaymentRequest extends PaymentHeader {

    //RB08 -> SB08
    @Schema(description = "카드 구분", example = "C002")
    private String cardType;

    //RB09 -> SB09
    @Schema(description = "카드 번호", example = "374695******006")
    private String cardNumber;

    //RB10 -> SB10
    @Schema(description = "카드 유효기간 (YYMM)", example = "2910")
    private String expiryDate;

    //RB11 -> SB11
    @Schema(description = "CVC 혹은 4DBC", example = "000")
    private String cvc;

    //RB12 -> SB12
    @Schema(description = "카드 명의자", example = "**********          ")
    private String holderName;

    //RB13 -> SB13
    @Schema(description = "국가 코드", example = "KR")
    private String twoLetterCountryCode;

    //RB14
    @Schema(description = "3D Secure 구분 (Y: 3DS 1.0 / Z: 3DS 2.0 / N: 비인증 / V: eWallet)", example = "N")
    private String secure3D;

    //RB15
    @Schema(description = "CAVV (3D Secure 인증값)", example = " ")
    private String cavv;

    //RB16
    @Schema(description = "XID (3D Secure 트랜잭션 ID)", example = " ")
    private String xid;

    //RB17
    @Schema(description = "PARes 상태값", example = "Y")
    private String paresStatus;

    //RB18
    @Schema(description = "ECI 값", example = " ")
    private String eci;

    //RB25
    @Schema(description = "Dynamic Descriptor 명", example = " ")
    private String dynamicDesc;

    //RB26
    @Schema(description = "Dynamic Descriptor 연결처", example = " ")
    private String dynamicDescContact;

    //RB90
    @Schema(description = "가맹점 Ref", example = " ")
    public String merchantRef;

    //RB92
    @Schema(description = "고객명", example = " ")
    public CharSequence buyerName;
}
