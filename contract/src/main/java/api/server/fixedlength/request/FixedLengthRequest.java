package api.server.fixedlength.request;


import api.server.fixedlength.enmus.CommonHeaderType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "FixedLength EAI 요청")
public class FixedLengthRequest {

    @Schema(description = "Gram ID", example = "TEST001", required = true)
    private String gramId;

    @Schema(description = "Service ID", example = "TEST001_S", required = true)
    private String serviceId;

    @Schema(description = "입력 필드 데이터", example = "{\"id\": \"test001\", \"accNo\": \"5556666611\"}", required = true)
    private Map<String, String> inFields; // key-value 형태의 요청 바디

    @Schema(description = "입력 필드 사이즈", example = "20", hidden = true)
    @Setter
    private int inFieldLength;

    @Schema(
            description = "Header type of the request",
            example = "EAI",
            required = true,
            hidden = true,
            allowableValues = {"EAI", "BATCH", "MCI"}
    )
    @Setter
    private CommonHeaderType commonHeaderType;

}
