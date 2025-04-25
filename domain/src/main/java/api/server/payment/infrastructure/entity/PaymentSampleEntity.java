package api.server.payment.infrastructure.entity;

import api.server.payment.enums.PaymentSampleStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentSampleEntity {

    private String paymentId;
    private String merchantId;
    private String cardNumber;
    private Long amount;
    private PaymentSampleStatus status;
}
