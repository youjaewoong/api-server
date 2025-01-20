package api.server.common.helper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 날짜 포맷 유틸리티 클래스
 * 다양한 날짜 포맷팅 기능을 제공합니다.
 */
@UtilityClass
@Slf4j
public class DateFormatHelper {


    /**
     * 날짜 포맷 변환
     * @param dateStr 입력 날짜 문자열
     * @param currentFormat 현재 날짜 형식 (예: "yyyyMMdd")
     * @param desiredFormat 원하는 날짜 형식 (예: "YYYY-MM-DD" or "YYYY.MM.DD")
     * @return 변환된 날짜 문자열
     *
     * 추천 추가 날짜 형식:
     * - MM/dd/yyyy: 미국식 날짜 형식 (예: 01/20/2025)
     * - dd-MM-yyyy: 유럽식 날짜 형식 (예: 20-01-2025)
     * - EEEE, MMMM dd, yyyy: 요일과 월 이름 포함 (예: Monday, January 20, 2025)
     * - yyyy/MM/dd HH:mm:ss: 날짜와 시간 (예: 2025/01/20 15:45:30)
     * - yyyyMMdd'T'HHmmss: ISO 8601 기본 형식 (예: 20250120T154530)
     * - MMMM dd, yyyy 'at' h:mm a: 특정 포맷과 시간 포함 (예: January 20, 2025 at 3:45 PM)
     */
    public static String formatDate(String dateStr, String currentFormat, String desiredFormat) {
        try {
            SimpleDateFormat currentFormatter = new SimpleDateFormat(currentFormat);
            SimpleDateFormat desiredFormatter = new SimpleDateFormat(desiredFormat);
            Date date = currentFormatter.parse(dateStr);
            return desiredFormatter.format(date);
        } catch (Exception e) {
            log.error(e.getMessage());
            return "Invalid date format";
        }
    }

}