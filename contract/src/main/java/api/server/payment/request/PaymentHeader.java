package api.server.payment.request;

import api.server.common.helper.DateFormatHelper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PaymentHeader {

    //RH01
    @Schema(description = "전문버전", example = "v101")
    private String gramVersion;

    //RH02
    @Schema(description = "VEN ID", example = "KICC")
    private String msgId;

    //RH03, RH00 -> VD_MSGTYPE
    @Schema(description = "메시지타입", example = "0100")
    private String messageType;

    // 0000 -> VD_RESCODE
    @Schema(description = "응답코드", example = "0000")
    private String outCode;

    //RH05
    @Schema(description = "전송일시", example = "20250331000039")
    private String sentDateTime = DateFormatHelper.getCurrentSystemTime();

    //RB01 -> SB01
    @Schema(description = "가맹점 아이디", example = "10D06D934B")
    private String merchantId;

    //RB02 -> SB02
    @Schema(description = "PG MID (가맹점 번호)", example = "10D69W000129048525")
    private String pgMid;

    //RB03 -> VS_FIELD05
    @Schema(description = "통화 코드", example = "KRW")
    private String currency;

    //RB04 -> VD_AMT -> SB04
    @Schema(description = "거래 금액", example = "000000012600")
    private String amount;

    //RB05 -> SB05
    @Schema(description = "봉사료", example = "000000000000")
    private String tax;

    //RB06 -> SB06
    @Schema(description = "세금", example = "000000000000")
    private String serviceCharge;

    //RB07 -> SB07
    @Schema(description = "할부 기간", example = "00")
    private String installmentPeriod;
}
