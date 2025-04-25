package api.server.fixedlength.helper;

import api.server.common.annotation.FixedLength;
import api.server.fixedlength.vo.FixedLengthJsonVO;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@UtilityClass
public class FixedLengthHelper {

    /**
     * 오른쪽 공백 추가 로직.
     *
     * @param value  문자열 값
     * @param length 고정 길이
     * @return 고정 길이 문자열
     */
    private static String padRight(String value, int length) {
        if (value == null) {
            value = "";
        }
        int padding = length - value.length();
        if (padding > 0) {
            StringBuilder sb = new StringBuilder(length);
            sb.append(value);
            for (int i = 0; i < padding; i++) {
                sb.append(' ');
            }
            return sb.toString();
        } else {
            return value.substring(0, length); // 길이 초과 시 잘라내기
        }
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
     * 시스템 현재 날짜 및 시간 패턴 HHmmssSSSS 형식으로 반환합니다.
     */
    public static String getCurrentTimeFormat() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmssSSSS");
        return now.format(formatter);
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
            String value = data.getOrDefault(field.getFieldId(), ""); // 값이 없으면 빈 문자열
            sb.append(padRight(value, field.getLength())); // 직접 구현한 패딩 메서드 사용
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
                        sb.append(padRight(value, annotation.length())); // padRight로 대체
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Failed to access field: " + field.getName(), e);
                    }
                });

        return sb.toString();
    }


    /**
     * 객체를 고정 길이 문자열로 변환.
     *
     * @param inputString 대상 객체
     * @return 고정 길이 문자열
     */
    public static <T> T fromFixedLengthString(String inputString, Class<T> targetClass) {
        try {
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            T instance = constructor.newInstance(); // 객체 생성

            // 현재 클래스와 부모 클래스를 포함한 모든 필드를 처리
            Class<?> currentClass = targetClass;
            while (currentClass != null) {
                Field[] fields = currentClass.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(FixedLength.class)) {
                        processField(field, inputString, instance);
                    }
                }
                currentClass = currentClass.getSuperclass(); // 부모 클래스로 이동
            }

            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Error processing class: " + targetClass.getName(), e);
        }
    }


    private static void processField(Field field, String inputString, Object instance) {
        FixedLength fixedLength = field.getAnnotation(FixedLength.class);
        int offset = fixedLength.offset();
        int length = fixedLength.length();

        if (!isValidRange(offset, length, inputString.length())) {
            return;
        }

        String extractedValue = inputString.substring(offset, offset + length).trim();
        field.setAccessible(true); // 필드 접근 허용

        try {
            Object convertedValue = convertValue(extractedValue, field.getType());
            field.set(instance, convertedValue);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Error setting field value for: " + field.getName(), e);
        }
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static Object convertValue(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return parseInteger(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return parseLong(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return parseDouble(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == LocalDateTime.class) {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } else {
            throw new IllegalArgumentException("Unsupported target type: " + targetType.getName());
        }
    }

    private static boolean isValidRange(int offset, int length, int totalLength) {
        return offset >= 0 && length > 0 && (offset + length) <= totalLength;
    }

    // 숫자 변환 유틸리티: 숫자 변환 중 예외 처리 포함
    private static Integer parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    public int getTotalValueLength(Map<String, String> inFields) {
        return inFields.values().stream()
                .filter(Objects::nonNull)
                .mapToInt(String::length)
                .sum();
    }

}
