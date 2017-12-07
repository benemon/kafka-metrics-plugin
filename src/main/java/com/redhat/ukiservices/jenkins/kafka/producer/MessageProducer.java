/*
 * Copyright 2017, Ben Holmes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.ukiservices.jenkins.kafka.producer;

import com.redhat.ukiservices.jenkins.kafka.configuration.KafkaMetricsPluginConfig;
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
