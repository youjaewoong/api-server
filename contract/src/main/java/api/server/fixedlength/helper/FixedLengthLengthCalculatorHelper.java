package api.server.fixedlength.helper;

import java.util.List;

public class FixedLengthLengthCalculatorHelper {

    /**
     * 필드 리스트의 총 길이를 계산.
     *
     * @param fields 필드 리스트
     * @param <T>    필드 클래스
     * @return 총 길이
     */
    public static <T extends HasLength> int calculateTotalLength(List<T> fields) {
        return fields.stream()
                .mapToInt(HasLength::getLength)
                .sum();
    }


    public interface HasLength {
        int getLength();
    }
}
