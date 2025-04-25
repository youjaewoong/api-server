package api.server.payment.factory;

import api.server.payment.aggregate.PaymentAggregate;
import api.server.payment.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * PaymentAggregate 생성 책임을 갖는 팩토리
 */
@Component
public class PaymentFactory {
    public PaymentAggregate create(String merchantId, String cardNumber, Long amount) {
        return PaymentAggregate.builder()
                .paymentId(UUID.randomUUID().toString())
                .merchantId(merchantId)
                .cardNumber(cardNumber)
                .amount(amount)
                .status(PaymentStatus.CREATED)
                .build();
    }
}
