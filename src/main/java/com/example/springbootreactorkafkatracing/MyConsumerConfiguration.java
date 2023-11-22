package com.example.springbootreactorkafkatracing;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.MicrometerConsumerListener;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyConsumerConfiguration {

    @Bean
    public KafkaReceiver<String, String> kafkaReceiver(final MeterRegistry registry, final ObservationRegistry observationRegistry) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("security.protocol", "SSL");
        properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "pathtokeystoreifneeded");
        properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "abc");
        properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "pathtotruststoreifneeded");
        properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "abc");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "CHANGEMEHERE_KAFKAHOST:9092");
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "somegroup123");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "somegroup123");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        final ReceiverOptions<String, String> receiverOptions = ReceiverOptions.create(properties);
        return KafkaReceiver.create(receiverOptions
                .consumerListener(new MicrometerConsumerListener(registry))
                .withObservation(observationRegistry)
                .subscription(Collections.singleton("CHANGE_TOPIC_SOURCE")));
    }

}
