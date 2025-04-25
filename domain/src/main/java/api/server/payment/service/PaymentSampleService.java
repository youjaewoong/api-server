package api.server.payment.service;

import api.server.exception.custom.BusinessException;
import api.server.kafka.PaymentSampleEventPublisher;
import api.server.payment.aggregate.PaymentSampleAggregate;
import api.server.payment.enums.PaymentSampleStatus;
import api.server.payment.errors.PaymentErrorCode;
import api.server.payment.infrastructure.repository.PaymentSampleRepository;
import api.server.payment.request.PaymentSampleRequest;
import api.server.payment.response.PaymentSampleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentSampleService {

    private final PaymentSampleRepository repository;
    private final PaymentSampleEventPublisher eventPublisher;

    @Transactional
    public PaymentSampleResponse create(PaymentSampleRequest dto) {
        PaymentSampleAggregate sample = PaymentSampleAggregate.builder()
                .paymentId(UUID.randomUUID().toString())
                .merchantId(dto.getMerchantId())
                .cardNumber(dto.getCardNumber())
                .amount(dto.getAmount())
                .status(PaymentSampleStatus.APPROVED)
                .build();
        sample.approve();
        repository.save(sample);

        // kafka 이벤트 전송
        eventPublisher.publishCreated(sample);

        return PaymentSampleResponse.builder()
                .paymentId(sample.getPaymentId())
                .merchantId(sample.getMerchantId())
                .amount(sample.getAmount())
                .status(sample.getStatus().name())
                .build();
    }

    public PaymentSampleResponse get(String id) {
        return repository.findById(id)
                .map(sample -> PaymentSampleResponse.builder()
                        .paymentId(sample.getPaymentId())
                        .merchantId(sample.getMerchantId())
                        .amount(sample.getAmount())
                        .status(sample.getStatus().name())
                        .build())
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND, id));
    }

    public List<PaymentSampleResponse> getAll() {
        return repository.findAll().stream()
                .map(sample -> PaymentSampleResponse.builder()
                        .paymentId(sample.getPaymentId())
                        .merchantId(sample.getMerchantId())
                        .amount(sample.getAmount())
                        .status(sample.getStatus().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateAmount(String id, Long newAmount) {
        PaymentSampleAggregate sample = repository.findById(id)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND, id));
        sample.updateAmount(newAmount);
        repository.update(sample);
    }

    @Transactional
    public void cancel(String id) {
        PaymentSampleAggregate sample = repository.findById(id)
                .orElseThrow(() -> new BusinessException(PaymentErrorCode.PAYMENT_NOT_FOUND, id));
        sample.cancel();
        repository.update(sample);
    }

    @Transactional
    public void delete(String id) {
        repository.delete(id);
    }
}
