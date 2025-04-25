package api.server.payment.aggregate;

import api.server.exception.custom.BusinessException;
import api.server.payment.enums.PaymentSampleStatus;
import api.server.payment.enums.PaymentStatus;
import api.server.payment.errors.PaymentErrorCode;
import api.server.payment.event.PaymentSampleCreatedEvent;
import lombok.Builder;
import lombok.Getter;

/**
 * 도메인 규칙을 캡슐화한 결제 Aggregate
 */
@Getter
@Builder
public class PaymentSampleAggregate {

    private final String paymentId;
    private final String merchantId;
    private final String cardNumber;
    private Long amount;
    private PaymentSampleStatus status;

    public void approve() {
        if (amount == null || amount <= 0) {
            throw new BusinessException(PaymentErrorCode.CURRENCY_MISMATCH);
        }
        this.status = PaymentSampleStatus.APPROVED;
    }

    public void cancel() {
        if (!PaymentSampleStatus.APPROVED.equals(this.status)) {
            throw new BusinessException(PaymentErrorCode.CURRENCY_MISMATCH);
        }
        this.status = PaymentSampleStatus.CANCELLED;
    }

    public void updateAmount(Long newAmount) {
        if (newAmount == null || newAmount <= 0) {
            throw new BusinessException(PaymentErrorCode.CURRENCY_MISMATCH);
        }
        this.amount = newAmount;
    }

    public PaymentSampleCreatedEvent toCreatedEvent() {
        return PaymentSampleCreatedEvent.builder()
                .paymentId(this.paymentId)
                .merchantId(this.merchantId)
                .amount(this.amount)
                .status(this.status.name())
                .build();
    }
}