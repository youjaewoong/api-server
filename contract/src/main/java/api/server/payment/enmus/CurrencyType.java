package api.server.payment.enmus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = "통화 코드 (Currency Code)")
public enum CurrencyType {

    @Schema(description = "대한민국 원화")
    KRW("대한민국 원화"),

    @Schema(description = "미국 달러")
    USD("미국 달러"),

    @Schema(description = "일본 엔화")
    JPY("일본 엔화"),

    @Schema(description = "태국 바트")
    THB("태국 바트"),

    @Schema(description = "홍콩 달러")
    HKD("홍콩 달러");

    private final String description;

}
