package api.server.payment.request;

import api.server.common.helper.DateFormatHelper;
import api.server.payment.enmus.CurrencyType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PaymentHeader {

    //RH01
    @Schema(description = "전문버전", example = "1000")
    private String gramVersion;

    //RH02
    @Schema(description = "VEN ID", example = "KICC")
    private String msgId;

    //RH03
    @Schema(description = "메시지타입", example = "0100")
    private String messageType;

    //RH05
    @Schema(description = "전송일시", example = "20250331000039")
    private String sentDateTime = DateFormatHelper.getCurrentSystemTime();

    //RB01
    @Schema(description = "가맹점 아이디", example = "10D06D934B")
    private String storeId;

    //RB02
    @Schema(description = "PG MID (가맹점 번호)", example = "10D69W000129048525")
    private String pgMid;

    //RB03
    @Schema(description = "통화 코드", example = "KRW")
    private CurrencyType currencyType;

    //RB04
    @Schema(description = "거래 금액", example = "000000012600")
    private String transactionAmount;

    //RB05
    @Schema(description = "봉사료", example = "000000000000")
    private String tax;

    //RB06
    @Schema(description = "세금", example = "000000000000")
    private String serviceCharge;

    //RB07
    @Schema(description = "할부 기간", example = "00")
    private String installmentPeriod;
}
