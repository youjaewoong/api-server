package api.server.fixedlength.cache;

import api.server.fixedlength.helper.FixedLengthJsonLoaderHelper;
import api.server.fixedlength.vo.FixedLengthJsonVO;
import lombok.experimental.UtilityClass;

import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class FixedLengthJsonCache {

    private static final ConcurrentHashMap<String, FixedLengthJsonVO> cache = new ConcurrentHashMap<>();

    /**
     * JSON 데이터를 캐싱하거나 로드.
     *
     * @param filePath 파일 경로
     * @return 캐싱된 JSON 데이터
     */
    public static FixedLengthJsonVO getJson(String filePath) {
        return cache.computeIfAbsent(filePath, path -> FixedLengthJsonLoaderHelper.loadJson(path, FixedLengthJsonVO.class));
    }
}
