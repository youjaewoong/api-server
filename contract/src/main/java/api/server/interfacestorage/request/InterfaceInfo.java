package api.server.interfacestorage.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전문정보 포맷 요청정보")
public class InterfaceInfo {

    /** 전문 ID (예: TEST001) */
    private String interfaceId;

    /** 전뭉 명 (예: 테스트001) */
    private String interfaceName;

    /** IN 필드 리스트 */
    private List<FieldInfo> inFields;

    /** OUT 필드 리스트 */
    private List<FieldInfo> outFields;

    /** IN 필드 총 길이 */
    private int totalInLength;

    /** OUT 필드 총 길이 */
    private int totalOutLength;

}
