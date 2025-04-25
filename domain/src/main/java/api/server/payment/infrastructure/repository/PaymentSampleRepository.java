package api.server.payment.infrastructure.repository;

import api.server.payment.aggregate.PaymentSampleAggregate;

import java.util.List;
import java.util.Optional;

public interface PaymentSampleRepository {
    void save(PaymentSampleAggregate aggregate);
    Optional<PaymentSampleAggregate> findById(String paymentId);
    void update(PaymentSampleAggregate aggregate);
    void delete(String paymentId);
    List<PaymentSampleAggregate> findAll();
}
