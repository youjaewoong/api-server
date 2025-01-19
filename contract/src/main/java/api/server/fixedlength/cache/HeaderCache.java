package api.server.fixedlength.cache;

import api.server.fixedlength.header.Header;
import api.server.fixedlength.header.HeaderFactory;
import api.server.fixedlength.request.FixedLengthRequest;
import lombok.experimental.UtilityClass;

import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class HeaderCache {

    // 캐싱된 헤더 데이터를 저장하는 맵
    private static final ConcurrentHashMap<String, String> headerCacheMap = new ConcurrentHashMap<>();

    /**
     * 헤더 데이터를 캐싱하거나 생성 팩토리패턴 으로 해더 구분 처리
     *
     * @param type    요청 유형
     * @param request 요청 데이터
     * @return 고정 길이 헤더 데이터
     */
    public static String getHeader(String type, FixedLengthRequest request) {
        return headerCacheMap.computeIfAbsent(type, key -> {
            Header headerBuilder = HeaderFactory.getHeaderBuilder(type, request);
            return headerBuilder.buildHeader(request);
        });
    }
}
