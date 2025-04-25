package api.server.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentSampleStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED");

    private final String value;
}
