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

    private Map<String, Object> inData;
}
