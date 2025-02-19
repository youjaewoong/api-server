package api.server.restapi.response.common;

import api.server.restapi.response.RestAPIBasicResponse;
import api.server.restapi.response.lottecard.TEST001Response;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@UtilityClass
public class RestAPIResponseFormatterFactory {

    // gramId와 해당 ResponseFormatter 생성 로직을 매핑하는 맵
    private static final Map<String, Supplier<RestAPIResponseFormatter>> RESPONSE_FORMATTER_MAP = new HashMap<>();

    static {
        // gramId와 대응하는 ResponseFormatter 등록
        RESPONSE_FORMATTER_MAP.put("TEST001", TEST001Response::new);
    }


    /**
     * <pre>
     * 포맷팅 처리 및 Object 구조로 반환 합니다.
     * FixedLengthBasicResponse 기본 구조로 리턴 처리 합니다.
     * </pre>
     *
     * @param gramId 전문 ID
     * @return 응답 객체
     */
    public static RestAPIResponseFormatter getFormatter(String gramId) {
        return RESPONSE_FORMATTER_MAP.getOrDefault(gramId, RestAPIBasicResponse::new).get();
    }
}
