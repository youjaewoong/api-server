package api.server.fixedlength.header;


import api.server.fixedlength.builder.LottecardHeaderBuilder;
import api.server.fixedlength.vo.CustomHeaderVO;
import api.server.fixedlength.vo.LottecardCommonHeaderVO;
import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class HeaderFactory {

    // Map의 값 타입을 Class<? extends Header>로 명시적으로 선언
    private static final Map<String, Class<? extends HeaderType>> headerMap = new HashMap<>();

    static {
        // 헤더 클래스 등록
        headerMap.put("lottecard", LottecardCommonHeaderVO.class);
        headerMap.put("custom", CustomHeaderVO.class);
    }

    /**
     * 지정된 타입에 해당하는 헤더 객체를 반환.
     *
     * @param type 헤더 타입 (e.g., "lottecard", "custom")
     * @return 생성된 헤더 객체
     */
    public static HeaderType getHeader(String type) {
        Class<? extends HeaderType> voClass = headerMap.getOrDefault(type, LottecardCommonHeaderVO.class);
        try {
            return voClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create Header object for type: " + type, e);
        }
    }


    public static Header getHeaderBuilder(String type) {
        if (type.equals("lottecard")) {
            return new LottecardHeaderBuilder();
        }
        throw new IllegalArgumentException("Unsupported header type: " + type);
    }
}
