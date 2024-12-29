package api.server.fixedlength.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Builder
@Schema(description = "FixedLength 응답")
public class FixedLengthResponse implements Serializable {

	private Map<String, Object> result;
}
