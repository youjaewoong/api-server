package api.server.fixedlength.helper;

import api.server.common.enums.GramType;
import api.server.fixedlength.vo.FixedLengthJsonVO;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


/**
 * Fixed-length 데이터를 JSON 형태로 매핑하는 유틸 클래스.
 */
@Slf4j
@UtilityClass
public class FixedLengthJsonLoaderHelper {

    /**
     * Fixed-length 데이터를 JSON 형태의 Map으로 변환하는 메서드.
     *
     * @param fixedLengthData 고정 길이 데이터 문자열
     * @param outFields       변환할 필드 정보 리스트
     * @return JSON 형식으로 변환된 데이터 (Map 형태)
     */
    public static Map<String, Object> mapFixedLengthToJson(String fixedLengthData, List<FixedLengthJsonVO.Field> outFields) {
        Map<String, Object> mappedData = new HashMap<>();

        for (int currentIndex = 0; currentIndex < outFields.size(); currentIndex++) {
            FixedLengthJsonVO.Field field = outFields.get(currentIndex);

            if (GramType.L.equals(field.getGramType())) {
                // 리스트 처리 (반복 필드가 있는 경우)
                List<Map<String, Object>> listData = new ArrayList<>();
                int repeatCount = getRepeatCount(fixedLengthData, field);
                mappedData.put(field.getFieldId(), extractFieldValue(fixedLengthData, field, field.getOffset()));

                // 반복될 필드 그룹을 설정
                List<FixedLengthJsonVO.Field> groupFields = new ArrayList<>();
                int nextIndex = currentIndex + 1;
                while (nextIndex < outFields.size() && !GramType.L.equals(outFields.get(nextIndex).getGramType())) {
                    groupFields.add(outFields.get(nextIndex));
                    nextIndex++;
                }

                // 기본 오프셋 설정
                int baseOffset = field.getOffset() + field.getLength();
                int groupLength = getTotalGroupLength(groupFields);

                // 반복문 실행 시 offset을 누적하여 사용
                for (int i = 0; i < repeatCount; i++) {
                    int offset = baseOffset + (i * groupLength);

                    if (offset + groupLength <= fixedLengthData.length()) {
                        listData.add(extractGroupData(fixedLengthData, groupFields, offset));
                    } else {
                        System.err.println("Invalid offset: " + offset + " for field " + field.getFieldId());
                    }
                }

                mappedData.put(field.getFieldId() + "List", listData);
                currentIndex = nextIndex - 1;
            } else {
                mappedData.put(field.getFieldId(), extractFieldValue(fixedLengthData, field, field.getOffset()));
            }
        }
        return mappedData;
    }

    /**
     * 반복 횟수를 가져오는 메서드.
     *
     * @param fixedLengthData 원본 고정 길이 데이터
     * @param field           현재 필드 정보
     * @return 반복 횟수 (정수 값)
     */
    private static int getRepeatCount(String fixedLengthData, FixedLengthJsonVO.Field field) {
        String value = Optional.ofNullable(extractFieldValue(fixedLengthData, field, field.getOffset())).map(Object::toString).orElse("0");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 필드 값을 추출하는 메서드.
     *
     * @param fixedLengthData 원본 고정 길이 데이터
     * @param field           현재 필드 정보
     * @return 추출된 필드 값 (String 또는 Integer)
     */
    private static Object extractFieldValue(String fixedLengthData, FixedLengthJsonVO.Field field, int offset) {
        int start = offset;
        int end = Math.min(start + field.getLength(), fixedLengthData.length());
        String value = fixedLengthData.substring(start, end).trim();

        if (GramType.N.equals(field.getGramType())) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return value;
    }

    /**
     * 그룹 데이터를 추출하여 Map 형태로 반환하는 메서드.
     *
     * @param fixedLengthData 원본 고정 길이 데이터
     * @param groupFields     그룹에 포함될 필드 리스트
     * @return 그룹 데이터를 포함한 Map 객체
     */
    private static Map<String, Object> extractGroupData(String fixedLengthData, List<FixedLengthJsonVO.Field> groupFields, int baseOffset) {
        Map<String, Object> groupData = new HashMap<>();
        int currentOffset = baseOffset;
        for (FixedLengthJsonVO.Field field : groupFields) {
            groupData.put(field.getFieldId(), extractFieldValue(fixedLengthData, field, currentOffset));
            currentOffset += field.getLength();
        }
        return groupData;
    }

    /**
     * 그룹 필드들의 총 길이를 계산하는 메서드.
     *
     * @param groupFields 그룹에 포함될 필드 리스트
     * @return 그룹 전체 길이 (int)
     */
    private static int getTotalGroupLength(List<FixedLengthJsonVO.Field> groupFields) {
        return groupFields.stream().mapToInt(FixedLengthJsonVO.Field::getLength).sum();
    }
}