package api.server.common.helper;

import api.server.payment.errors.CardErrorCode;
import api.server.exception.custom.BusinessException;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Set;

public class CardNumberHelper {
    private static final String MASK_PATTERN = "******";
    private static final int VISIBLE_PREFIX_LENGTH = 6;
    private static final int VISIBLE_SUFFIX_LENGTH = 3;
    private static final String DEFAULT_CVV_MASK = "000";

    private static final Set<String> MESSAGE_TYPES_WITH_CVV_MASK =
            Set.of("0100", "0101", "0121", "0181", "0182", "0184");

    @JsonValue
    private final String value;

    private CardNumberHelper(String number) {
        validateCardNumber(number);
        this.value = number;
    }

    public static CardNumberHelper of(String number) {
        return new CardNumberHelper(number);
    }

    public String getMaskedNumber() {
        validateMaskingLength();
        return generateMaskedNumber();
    }

    public String getMaskedForMessageType(String messageType) {
        if (MESSAGE_TYPES_WITH_CVV_MASK.contains(messageType)) {
            return getMaskedNumber() + DEFAULT_CVV_MASK;
        } else if ("0180".equals(messageType)) {
            return getMaskedNumber();
        }
        return value;
    }

    public String getOriginalNumber() {
        return value;
    }

    private void validateCardNumber(String number) {
        if (number == null || !number.matches("\\d{15,16}")) {
            throw new BusinessException(CardErrorCode.INVALID_NUMBER);
        }
    }

    private void validateMaskingLength() {
        if (value.length() < VISIBLE_PREFIX_LENGTH + VISIBLE_SUFFIX_LENGTH) {
            throw new BusinessException(CardErrorCode.INVALID_NUMBER);
        }
    }

    private String generateMaskedNumber() {
        String prefix = value.substring(0, VISIBLE_PREFIX_LENGTH);
        String suffix = value.substring(value.length() - VISIBLE_SUFFIX_LENGTH);
        return prefix + MASK_PATTERN + suffix;
    }
}