package com.example.springbootreactorkafkatracing;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.observation.KafkaReceiverObservation;
import reactor.kafka.receiver.observation.KafkaRecordReceiverContext;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;


@Service
public class SpringBootReactorKafkaTracingApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringBootReactorKafkaTracingApplicationService.class);

    @Autowired
    KafkaReceiver<String, String> kafkaReceiver;

    @Autowired
    KafkaSender<String, String> kafkaSender;

    @Autowired
    ObservationRegistry observationRegistry;

    Flux<SenderResult<String>> reactorKafkaPropagatesTraces() {
        Observation parentObservation = Observation.start("test parent observation", this.observationRegistry);
        return kafkaSender.send(
                this.kafkaReceiver
                        .receive()
                        .flatMap(record -> {
                            Observation receiverObservation =
                                    KafkaReceiverObservation.RECEIVER_OBSERVATION.start(null,
                                            KafkaReceiverObservation.DefaultKafkaReceiverObservationConvention.INSTANCE,
                                            () -> new KafkaRecordReceiverContext(record, "user.receiver", "myKafkaServer"),
                                            this.observationRegistry);

                            return Mono.just(record)
                                    .map(oneReceivedMessage -> {
                                        String transformedMessage = oneReceivedMessage.value();
                                        LOGGER.info("I WOULD LIKE TO SEE DIFFERENT TRACES HERE AS WELL PLEASE" + transformedMessage);
                                        return transformedMessage.toUpperCase();
                                    })
                                    .tap(Micrometer.observation(observationRegistry))
                                    .doOnTerminate(receiverObservation::stop)
                                    .doOnError(receiverObservation::error)
                                    .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, receiverObservation));
                        })
                        .map(oneTransformedReceivedMessage -> SenderRecord.create(new ProducerRecord<>("CHANGE_TOPIC_DESTINATION", null, oneTransformedReceivedMessage), oneTransformedReceivedMessage))
                )
                .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, parentObservation))
                ;

    }

}
