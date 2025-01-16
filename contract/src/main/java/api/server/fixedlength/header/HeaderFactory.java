package api.server.fixedlength.header;


import api.server.common.constant.CompanyConstant;
import api.server.fixedlength.builder.LottecardBatchHeaderBuilder;
import api.server.fixedlength.builder.LottecardHeaderBuilder;
import api.server.fixedlength.enmus.CommonHeaderType;
import api.server.fixedlength.request.FixedLengthRequest;
import api.server.fixedlength.vo.CustomHeaderVO;
import api.server.fixedlength.vo.LottecardCommonHeaderVO;
import lombok.experimental.UtilityClass;


import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class HeaderFactory {

    // Map의 값 타입을 Class<? extends Header>로 명시적으로 선언
    private static final Map<String, Class<? extends HeaderType>> headerMap = new HashMap<>();
    private static final EnumMap<CommonHeaderType, Class<? extends Header>> headerBuilderMap = new EnumMap<>(CommonHeaderType.class);

    static {
        // 헤더 클래스 등록
        headerMap.put(CompanyConstant.LOTTE_CARD, LottecardCommonHeaderVO.class);
        headerMap.put(CompanyConstant.CUSTOM, CustomHeaderVO.class);

        //해더 빌더 등록
        headerBuilderMap.put(CommonHeaderType.BATCH, LottecardBatchHeaderBuilder.class);
        headerBuilderMap.put(CommonHeaderType.EAI, LottecardHeaderBuilder.class);
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


    /**
     * 지정된 타입에 해당하는 헤더빌더 객체를 반환.
     *
     * @param type 헤더 타입 (e.g., "lottecard", "custom")
     * @return 생성된 헤더빌더 객체
     */
    public static Header getHeaderBuilder(String type, FixedLengthRequest request) {
        try {
            if (type.equals(CompanyConstant.LOTTE_CARD)) {
                Class<? extends Header> voClass =
                        headerBuilderMap.getOrDefault(request.getCommonHeaderType(), LottecardHeaderBuilder.class);
                return voClass.getDeclaredConstructor().newInstance();
            }
            throw new NullPointerException("is null HeaderBuilder: " + type);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create HeaderBuilder object for type: " + type, e);
        }

    }
}
