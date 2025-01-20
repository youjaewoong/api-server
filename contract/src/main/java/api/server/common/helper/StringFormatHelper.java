package api.server.common.helper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Stack;

/**
 * 문자열 포맷 유틸리티 클래스
 * 다양한 문자열 포맷팅 기능을 제공합니다.
 */
@UtilityClass
@Slf4j
public class StringFormatHelper {


    /**
     * 전화번호 포맷 변환
     * @param phoneNumber 전화번호 (숫자만 포함된 문자열)
     * @return 포맷된 전화번호 (예: 010-1234-5678)
     */
    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || (!phoneNumber.matches("\\d{10,11}") && !phoneNumber.matches("\\d{3}-\\d{3,4}-\\d{4}"))) {
            log.error("Invalid phone number: {}", phoneNumber);
            return phoneNumber;
        }
        String cleanedNumber = phoneNumber.replaceAll("[^\\d]", "");
        if (cleanedNumber.length() == 11) {
            return cleanedNumber.replaceAll("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
        } else if (cleanedNumber.length() == 10) {
            return cleanedNumber.replaceAll("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }
        log.error("Invalid phone number: {}", phoneNumber);
        return phoneNumber;
    }


    /**
     * 값 존재 여부로 Y/N 반환
     * @param value 체크할 값
     * @return 값이 null 이거나 빈 문자열이면 "N", 그렇지 않으면 "Y"
     */
    public static String getYesOrNo(String value) {
        return (value == null || value.trim().isEmpty()) ? "N" : "Y";
    }


}