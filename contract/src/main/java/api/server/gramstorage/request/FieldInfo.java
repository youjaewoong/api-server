package api.server.gramstorage.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전문필드 요청정보")
public class FieldInfo {

    /** 필드 ID */
    private String fieldId;

    /**  필드 명 */
    private String fieldName;

    /** 필드 데이터 길이 */
    private int length;

    /** 필드 데이터 오프셋 (시작 위치) */
    private int offset;
}
