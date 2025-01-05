package api.server.fixedlength.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "FixedLength 요청")
public class FixedLengthRequest {

    @Schema(description = "Gram ID", example = "TEST01", required = true)
    private String gramId;

    @Schema(description = "Service ID", example = "TEST01_S", required = true)
    private String serviceId;

    @Schema(description = "입력 필드 데이터", example = "{\"id\": \"test001\", \"accNo\": \"5556666611\"}", required = true)
    private Map<String, String> inFields; // key-value 형태의 요청 바디
}
