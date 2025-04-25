package api.server.payment.infrastructure.mybatis;

import api.server.payment.infrastructure.entity.PaymentSampleEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * PaymentSampleMybatisMapper.java (MyBatis 인터페이스)
 */
@Mapper
public interface PaymentSampleMybatisMapper {
    void insert(PaymentSampleEntity entity);
    PaymentSampleEntity findById(String paymentId);
    void update(PaymentSampleEntity entity);
    void delete(String paymentId);
    List<PaymentSampleEntity> findAll();
}