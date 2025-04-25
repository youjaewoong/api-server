package api.server.payment.aggregate;

import api.server.exception.custom.BusinessException;
import api.server.payment.enums.PaymentStatus;
import api.server.payment.errors.PaymentErrorCode;
import lombok.Builder;
import lombok.Getter;

/**
 * 도메인 규칙을 캡슐화한 결제 Aggregate
 */
@Getter
public class PaymentAggregate {

    private final String paymentId;
    private final String merchantId;
    private final String cardNumber;
    private final Long amount;
    private PaymentStatus status;

    @Builder
    public PaymentAggregate(String paymentId, String merchantId, String cardNumber, Long amount, PaymentStatus status) {
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.status = status;
    }

    /**
     * 결제 승인 처리
     */
    public void approve() {
        if (amount == null || amount <= 0) {
            throw new BusinessException(PaymentErrorCode.CURRENCY_MISMATCH);
        }
        this.status = PaymentStatus.APPROVED;
    }

    /**
     * 결제 취소 처리
     */
    public void cancel() {
        if (!PaymentStatus.APPROVED.equals(this.status)) {
            throw new BusinessException(PaymentErrorCode.CURRENCY_MISMATCH);
        }
        this.status = PaymentStatus.CANCELLED;
    }
}