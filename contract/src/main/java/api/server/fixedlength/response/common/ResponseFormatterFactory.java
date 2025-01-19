package api.server.fixedlength.response.common;

import api.server.fixedlength.response.FixedLengthBasicResponse;
import api.server.fixedlength.response.lottecard.TEST001Response;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@UtilityClass
public class ResponseFormatterFactory {

    // gramId와 해당 ResponseFormatter 생성 로직을 매핑하는 맵
    private static final Map<String, Supplier<ResponseFormatter>> RESPONSE_FORMATTER_MAP = new HashMap<>();

    static {
        // gramId와 대응하는 ResponseFormatter 등록
        RESPONSE_FORMATTER_MAP.put("TEST001", TEST001Response::new);
    }

    public static ResponseFormatter getFormatter(String gramId) {
        return RESPONSE_FORMATTER_MAP.getOrDefault(gramId, FixedLengthBasicResponse::new).get();
    }
}
