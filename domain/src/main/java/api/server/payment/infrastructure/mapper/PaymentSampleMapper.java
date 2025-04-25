package api.server.payment.infrastructure.mapper;

import api.server.payment.aggregate.PaymentSampleAggregate;
import api.server.payment.infrastructure.entity.PaymentSampleEntity;

public class PaymentSampleMapper {

    public static PaymentSampleEntity toEntity(PaymentSampleAggregate agg) {
        return PaymentSampleEntity.builder()
                .paymentId(agg.getPaymentId())
                .merchantId(agg.getMerchantId())
                .cardNumber(agg.getCardNumber())
                .amount(agg.getAmount())
                .status(agg.getStatus())
                .build();
    }

    public static PaymentSampleAggregate toAggregate(PaymentSampleEntity entity) {
        return PaymentSampleAggregate.builder()
                .paymentId(entity.getPaymentId())
                .merchantId(entity.getMerchantId())
                .cardNumber(entity.getCardNumber())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .build();
    }
}
