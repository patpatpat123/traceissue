package com.example.springbootreactorkafkatracing;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.MicrometerProducerListener;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyProducerConfiguration {

    @Bean
    public KafkaSender<String, String> kafkaSender(final MeterRegistry registry, final ObservationRegistry observationRegistry) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("security.protocol", "SSL");
        properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "pathtokeystoreifneeded");
        properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "abc");
        properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "pathtotruststoreifneeded");
        properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "abc");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "CHANGEMEHERE_KAFKAHOST:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        final SenderOptions<String, String> senderOptions = SenderOptions.create(properties);
        return KafkaSender.create(senderOptions
                .withObservation(observationRegistry)
                .producerListener(new MicrometerProducerListener(registry)));
    }

}
