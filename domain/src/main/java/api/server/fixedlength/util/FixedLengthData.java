package api.server.fixedlength.util;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class FixedLengthData {

    // 필드와 길이 정의
    private static final Map<String, Integer> FIELD_LENGTHS = new HashMap<>() {{
        put("userId", 10);
        put("cardNumber", 20);
    }};
}
