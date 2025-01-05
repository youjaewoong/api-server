package api.server.fixedlength.helper;

import api.server.fixedlength.vo.FixedLengthJsonVO;
import lombok.experimental.UtilityClass;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class FixedLengthJsonLoaderHelper {

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
            throw new RuntimeException("Failed to load JSON file: " + filePath, e);
        }
    }


    /**
     * 고정 길이 데이터를 JSON으로 매핑.
     *
     * @param fixedLengthData 고정 길이 데이터
     * @param outFields       JSON 파일의 outFields 정의
     * @return 매핑된 JSON 데이터
     */
    public static Map<String, String> mapFixedLengthToJson(String fixedLengthData, List<FixedLengthJsonVO.Field> outFields) {
        Map<String, String> mappedData = new HashMap<>();

        for (FixedLengthJsonVO.Field field : outFields) {
            int start = field.getOffset();
            int end = start + field.getLength();
            String value = fixedLengthData.substring(start, end).trim();
            mappedData.put(field.getFieldId(), value);
        }

        return mappedData;
    }
}
