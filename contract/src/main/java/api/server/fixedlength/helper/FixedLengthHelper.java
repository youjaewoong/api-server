package api.server.fixedlength.helper;

import api.server.common.annotation.FixedLength;
import api.server.fixedlength.vo.FixedLengthJsonVO;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;

@UtilityClass
public class FixedLengthHelper {

    public static String padRight(String input, int length) {
        return String.format("%-" + length + "s", input == null ? "" : input);
    }


    /**
     * 길이만큼 공백으로 채움
     * @param length  고정 길이
     * @return 지정된 길이의 문자열
     */
    public static String padWithSpaces(int length) {

        StringBuilder padded = new StringBuilder();
        while (padded.length() < length) {
            padded.append(" "); // 오른쪽에 공백 추가
        }
        return padded.substring(0, length); // 길이를 초과하지 않도록 자름
    }


    /**
     * 왼쪽에 0을 추가해 고정된 길이를 만듭니다.
     *
     * @param input  입력 문자열
     * @param length 고정된 길이
     * @return 패딩된 문자열
     */
    public static String padLeft(String input, int length) {
        if (input == null) {
            input = "";
        }
        StringBuilder padded = new StringBuilder(input);
        while (padded.length() < length) {
            padded.insert(0, '0'); // 왼쪽에 0 추가
        }
        return padded.toString();
    }


    /**
     * 시스템 현재 시간을 기본 형식(yyyyMMddHHmmss)으로 제공합니다.
     */
    public static String getCurrentSystemTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }


    /**
     * 시스템 현재 날짜를 기본 형식(yyyyMMdd)으로 제공합니다.
     */
    public static String getCurrentSystemDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }


    /**
     * 현재 시간을 yyyyMMddHHmmssSSS 형식으로 반환합니다.
     * @return 현재 시간 문자열 (yyyyMMddHHmmssSSS 형식)
     */
    public static String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return LocalDateTime.now().format(formatter);
    }


    /**
     * 요청 바디 데이터를 고정 길이 문자열로 생성.
     *
     * @param fields JSON 파일의 inFields 정의
     * @param data   매핑할 데이터
     * @return 고정 길이 문자열
     */
    public static String toFixedLengthBody(List<FixedLengthJsonVO.Field> fields, Map<String, String> data) {
        StringBuilder sb = new StringBuilder();

        for (FixedLengthJsonVO.Field field : fields) {
            String value = data.getOrDefault(field.getFieldId(), "");
            sb.append(String.format("%-" + field.getLength() + "s", value));
        }

        return sb.toString();
    }

    /**
     * 객체를 고정 길이 문자열로 변환.
     *
     * @param obj 대상 객체
     * @return 고정 길이 문자열
     */
    public static String toFixedLengthString(Object obj) {
        StringBuilder sb = new StringBuilder();

        Field[] fields = obj.getClass().getDeclaredFields();
        Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(FixedLength.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(FixedLength.class).offset()))
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        FixedLength annotation = field.getAnnotation(FixedLength.class);
                        String value = (String) field.get(obj);
                        if (value == null) value = "";
                        sb.append(String.format("%-" + annotation.length() + "s", value));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to access field: " + field.getName(), e);
                    }
                });

        return sb.toString();
    }

}
