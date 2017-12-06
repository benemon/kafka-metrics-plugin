package com.redhat.ukiservices.bholmes.jenkins.producer;

import com.redhat.ukiservices.bholmes.jenkins.configuration.KafkaMetricsPluginConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.logging.Logger;

public class MessageProducer implements AutoCloseable {

    private static final Logger log = Logger.getLogger(MessageProducer.class.getName());

    private static final String PROPERTY_WARN_MSG = "Could not instantiate Kafka Client. No data will be sent to Kafka! %n Caused by: %s";
    private static final String PRODUCER_WARN_MSG = "No Kafka Client configured. No data will be sent to Kafka.";

    private KafkaProducer producer;

    public MessageProducer() {
        try {
            Thread.currentThread().setContextClassLoader(null);
            producer = new KafkaProducer<String, String>(KafkaMetricsPluginConfig.get().getProperties());
        } catch (RuntimeException rte) {
            log.warning(String.format(PROPERTY_WARN_MSG, rte));
        }
    }

    /**
     * Send single line messages
     *
     * @param topic the topic to send the message to
     * @param body  the message payload
     */
    public void sendMessage(String topic, String body) {
        if (producer != null) {
            producer.send(new ProducerRecord<>(topic, body));
        } else {
            log.warning(PRODUCER_WARN_MSG);
        }
    }

    /**
     * Close the producer
     */
    @Override
    public void close() {
        if (producer != null) {
            producer.close();
        }
    }
}
