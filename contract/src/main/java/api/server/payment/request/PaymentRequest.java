package api.server.payment.request;

import api.server.payment.enmus.CurrencyType;
import api.server.payment.enmus.Secure3DType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "결제 요청")
public class PaymentRequest extends PaymentHeader {

    @Schema(description = "카드 구분", example = "C002")
    private String cardType;

    @Schema(description = "카드 번호", example = "374695******006")
    private String cardNumber;

    @Schema(description = "카드 유효기간 (YYMM)", example = "2910")
    private String expiryDate;

    @Schema(description = "CVC 혹은 4DBC", example = "000")
    private String cvc;

    @Schema(description = "국가 코드", example = "KR")
    private String twoLetterCountryCode;

    @Schema(description = "3D Secure 구분 (Y: 3DS 1.0 / Z: 3DS 2.0 / N: 비인증 / V: eWallet)", example = "N")
    private Secure3DType secure3D;

    @Schema(description = "CAVV (3D Secure 인증값)", example = " ")
    private String cavv;

    @Schema(description = "XID (3D Secure 트랜잭션 ID)", example = " ")
    private String xid;

    @Schema(description = "PARes 상태값", example = "Y")
    private String paresStatus;

    @Schema(description = "ECI 값", example = " ")
    private String eci;

    @Schema(description = "Dynamic Descriptor 명", example = " ")
    private String dynamicDesc;

    @Schema(description = "Dynamic Descriptor 연결처", example = " ")
    private String dynamicDescContact;
}
