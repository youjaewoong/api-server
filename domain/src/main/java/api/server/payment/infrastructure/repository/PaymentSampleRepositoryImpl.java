package api.server.payment.infrastructure.repository;

import api.server.payment.aggregate.PaymentSampleAggregate;
import api.server.payment.infrastructure.mapper.PaymentSampleMapper;
import api.server.payment.infrastructure.mybatis.PaymentSampleMybatisMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PaymentSampleRepositoryImpl implements PaymentSampleRepository {

    private final PaymentSampleMybatisMapper mapper;

    @Override
    public void save(PaymentSampleAggregate aggregate) {
        mapper.insert(PaymentSampleMapper.toEntity(aggregate));
    }

    @Override
    public Optional<PaymentSampleAggregate> findById(String paymentId) {
        return Optional.ofNullable(mapper.findById(paymentId))
                .map(PaymentSampleMapper::toAggregate);
    }

    @Override
    public void update(PaymentSampleAggregate aggregate) {
        mapper.update(PaymentSampleMapper.toEntity(aggregate));
    }

    @Override
    public void delete(String paymentId) {
        mapper.delete(paymentId);
    }

    @Override
    public List<PaymentSampleAggregate> findAll() {
        return mapper.findAll().stream()
                .map(PaymentSampleMapper::toAggregate)
                .collect(Collectors.toList());
    }
}
