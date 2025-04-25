package api.server.kafka;

import api.server.payment.aggregate.PaymentSampleAggregate;
import api.server.payment.event.PaymentSampleCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("local")
public class PaymentSampleEventPublisher {

    private final KafkaTemplate<String, PaymentSampleCreatedEvent> kafkaTemplate;

    public void publishCreated(PaymentSampleAggregate payment) {
        PaymentSampleCreatedEvent event = payment.toCreatedEvent();
        kafkaTemplate.send("payment.created", event);
    }
}