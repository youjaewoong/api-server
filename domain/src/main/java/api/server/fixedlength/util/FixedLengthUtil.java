package api.server.fixedlength.util;

import api.server.fixedlength.request.FixedLengthRequest;

import java.util.HashMap;
import java.util.Map;

public class FixedLengthUtil {


    public static String toFixedLength(String input, int length) {
        if (input == null) {
            input = "";
        }
        return String.format("%-" + length + "s", input);
    }


    public static String toFixedLength(Map<String, Object> data, Map<String, Integer> fieldLengths) {
        StringBuilder fixedLengthMessage = new StringBuilder();

        for (Map.Entry<String, Integer> entry : fieldLengths.entrySet()) {
            String field = entry.getKey();
            int length = entry.getValue();
            String value = data.getOrDefault(field, "").toString();

            // 필드를 고정 길이로 변환
            fixedLengthMessage.append(String.format("%-" + length + "s", value));
        }

        return fixedLengthMessage.toString();
    }

    public static Map<String, String> fromFixedLength(String message, Map<String, Integer> fieldLengths) {
        Map<String, String> parsedData = new HashMap<>();
        int currentIndex = 0;

        for (Map.Entry<String, Integer> entry : fieldLengths.entrySet()) {
            String field = entry.getKey();
            int length = entry.getValue();
            String value = message.substring(currentIndex, currentIndex + length).trim();
            parsedData.put(field, value);
            currentIndex += length;
        }

        return parsedData;
    }
}
