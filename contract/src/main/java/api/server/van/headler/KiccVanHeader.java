package api.server.van.headler;

import api.server.common.annotation.FixedLength;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@Schema(description = "KICC VAN 요청")
@NoArgsConstructor
public class KiccVanHeader {

    @Schema(description = "길이를 제외한 전문 총 길이", example = "0200")
    @Setter
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
}
