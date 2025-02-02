package api.server.fixedlength.cache;

import api.server.common.exception.custom.BusinessException;
import api.server.fixedlength.enmus.FixedLengthErrorCode;
import api.server.fixedlength.vo.FixedLengthJsonVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
@Slf4j
public class FixedLengthJsonCache {

    private static final ConcurrentHashMap<String, FixedLengthJsonVO> cache = new ConcurrentHashMap<>();

    /**
     * JSON 데이터를 캐싱하거나 로드.
     *
     * @param filePath 파일 경로
     * @return 캐싱된 JSON 데이터
     */
    public static FixedLengthJsonVO getJson(String filePath) {
        return cache.computeIfAbsent(filePath, path -> loadJson(path, FixedLengthJsonVO.class));
    }

    
    /**
     * JSON 파일을 읽어 동적으로 객체로 반환.
     *
     * @param filePath JSON 파일 경로
     * @param clazz    대상 클래스
     * @return 로드된 객체
     */
    public static <T> T loadJson(String filePath, Class<T> clazz) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(filePath), clazz);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BusinessException(FixedLengthErrorCode.JSON_LOAN_ERROR, filePath);
        }
    }
}
