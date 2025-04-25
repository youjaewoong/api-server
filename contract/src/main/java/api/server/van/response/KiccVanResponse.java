package api.server.van.response;

import api.server.common.annotation.FixedLength;
import api.server.van.headler.KiccVanHeader;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Schema(description = "KICC VAN 요청")
@SuperBuilder // 추가된 부분
@NoArgsConstructor
public class KiccVanResponse extends KiccVanHeader {

    @Schema(description = "1. 전문구분")
    @FixedLength(length = 4, offset = 127)
    private String messageType;

    @Schema(description = "2. VAN-TR")
    @FixedLength(length = 12, offset = 131)
    private String vanTr;

    @Schema(description = "3. Status - 'O': 정상 'P': 자동재결재 'X': 거절")
    @FixedLength(length = 1, offset = 143)
    private String status;

    @Schema(description = "4. 거래일시 - YYYYMMDDhhmmss (VAN 기준)")
    @FixedLength(length = 12, offset = 144)
    private String txDateTime;

    @Schema(description = "5. 카드번호")
    @FixedLength(length = 20, offset = 156)
    private String cardNumber;

    @Schema(description = "6. 유효기간")
    @FixedLength(length = 4, offset = 176)
    private String expireDate;

    @Schema(description = "7. 할부개월수")
    @FixedLength(length = 2, offset = 180)
    private String installmentMonths;

    @Schema(description = "8. 총금액")
    @FixedLength(length = 12, offset = 182)
    private String totalAmount;

    @Schema(description = "9. 메시지1")
    @FixedLength(length = 16, offset = 194)
    private String message1;

    @Schema(description = "10. 메시지2")
    @FixedLength(length = 16, offset = 210)
    private String message2;

    @Schema(description = "11. 메시지3")
    @FixedLength(length = 16, offset = 226)
    private String message3;

    @Schema(description = "12. 메시지4")
    @FixedLength(length = 16, offset = 242)
    private String message4;

    @Schema(description = "13. 승인번호")
    @FixedLength(length = 12, offset = 258)
    private String approvalNumber;

    @Schema(description = "14. 카드종류명")
    @FixedLength(length = 16, offset = 270)
    private String cardType;

    @Schema(description = "15. 발급사코드")
    @FixedLength(length = 2, offset = 286)
    private String issuerCode;

    @Schema(description = "16. 매입사코드")
    @FixedLength(length = 2, offset = 288)
    private String acquirerCode;

    @Schema(description = "17. 가맹점번호")
    @FixedLength(length = 15, offset = 290)
    private String merchantNumber;

    @Schema(description = "18. 전송구분")
    @FixedLength(length = 2, offset = 305)
    private String transmissionType;

    @Schema(description = "암호화다운로드시 Working Key Index(2byte) + Working Key(16) + Space(2)")
    @FixedLength(length = 20, offset = 307)
    private String notice;

    @Schema(description = "20. 발생포인트")
    @FixedLength(length = 12, offset = 327)
    private String occurredPoint;

    @Schema(description = "21. 가용포인트")
    @FixedLength(length = 12, offset = 339)
    private String availablePoint;

    @Schema(description = "22. 누적포인트")
    @FixedLength(length = 12, offset = 351)
    private String accumulatedPoint;

    @Schema(description = "23. 포인트카드사메시지")
    @FixedLength(length = 40, offset = 363)
    private String pointCardMessage;

    @Schema(description = "24. 가맹점사용ID")
    @FixedLength(length = 2, offset = 403)
    private String merchantUseId;

    @Schema(description = "25. 가맹점사용필드")
    @FixedLength(length = 30, offset = 405)
    private String merchantUseField;

    @Schema(description = "26. 여유필드")
    @FixedLength(length = 30, offset = 435)
    private String reservedField;
}
