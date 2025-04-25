package api.server.payment.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Schema(description = "3DS 암호화")
public enum Secure3DType {
    @Schema(description = "3D Secure 1.0")
    Y("3DS 1.0"),

    @Schema(description = "3D Secure 2.0")
    Z("3DS 2.0"),

    @Schema(description = "비인증")
    N("비인증"),

    @Schema(description = "eWallet 인증 (ex. 베트남)")
    V("eWallet");

    private final String description;
}
