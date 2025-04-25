package api.server.payment.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentSampleResponse {
    private String paymentId;
    private String merchantId;
    private Long amount;
    private String status;
}
