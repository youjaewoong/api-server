package api.server.restapi.request;

import api.server.restapi.vo.LottecardCommonHeaderVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Schema(description = "FixedLength EAI 요청")
public class RestAPIRequest extends LottecardCommonHeaderVO {

    @Schema(description = "입력 필드 데이터", example = "{\"id\": \"test001\", \"accNo\": \"5556666611\"}", required = true)
    private Map<String, String> inFields; // key-value 형태의 요청 바디

}
