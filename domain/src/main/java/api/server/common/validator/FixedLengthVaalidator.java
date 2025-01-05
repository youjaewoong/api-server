package api.server.common.validator;

import api.server.common.annotation.FixedLength;
import api.server.common.exception.custom.BusinessException;
import api.server.common.exception.enums.ErrorCode;

import java.lang.reflect.Field;

public class FixedLengthVaalidator {
    private static final String REQUIRED_MESSAGE = "%s is required but not provided";
    private static final String INVALID_LENGTH_MESSAGE = "%s must be of length %d";
    private static final String INVALID_OFFSET_MESSAGE = "Invalid offset for field %s. Expected: %d, Found: %d";

    public static void validate(Object obj) {
        Class<?> clazz = obj.getClass();
        int currentOffset = 0;

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(FixedLength.class)) {
                validateField(field, obj, currentOffset);
                currentOffset += field.getAnnotation(FixedLength.class).length();
            }
        }
    }

    private static void validateField(Field field, Object obj, int currentOffset) {
        FixedLength annotation = field.getAnnotation(FixedLength.class);
        field.setAccessible(true);

        try {
            String value = (String) field.get(obj);

            if (annotation.required() && (value == null || value.isEmpty())) {
                throwValidationException(ErrorCode.INVALID_PARAMETER,
                        String.format(REQUIRED_MESSAGE, field.getName()));
            }

            if (value != null && value.length() != annotation.length()) {
                throwValidationException(ErrorCode.INVALID_PARAMETER,
                        String.format(INVALID_LENGTH_MESSAGE, field.getName(), annotation.length()));
            }

            if (annotation.offset() != currentOffset) {
                throwValidationException(ErrorCode.INVALID_PARAMETER,
                        String.format(INVALID_OFFSET_MESSAGE, field.getName(), currentOffset, annotation.offset()));
            }
        } catch (IllegalAccessException e) {
            throwValidationException(ErrorCode.INVALID_PARAMETER,
                    String.format("Unable to access field: %s", field.getName()));
        }
    }
    private static void throwValidationException(ErrorCode errorCode, String message) {
        throw new BusinessException(errorCode, message);
    }
}
