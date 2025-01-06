package api.server.fixedlength.helper;

import api.server.fixedlength.vo.FixedLengthJsonVO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@UtilityClass
public class FixedLengthJsonLoaderHelper {


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
