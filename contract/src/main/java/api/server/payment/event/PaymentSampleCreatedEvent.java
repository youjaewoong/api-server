package api.server.payment.event;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PaymentSampleCreatedEvent {
    private String paymentId;
    private String merchantId;
    private Long amount;
    private String status;
}