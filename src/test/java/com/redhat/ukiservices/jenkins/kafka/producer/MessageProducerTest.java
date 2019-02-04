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

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import net.sf.json.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class MessageProducerTest {

    public static final String PRODUCER_TEST_TOPIC = "producer-test";
    @ClassRule
    public static final SharedKafkaTestResource kafkaRule = new SharedKafkaTestResource()
            .withBrokerProperty("port", "5001")
            .withBrokerProperty("host.name", "localhost");
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    private MessageProducer producer;

    @Before
    public void beforeTest() throws Exception {
        kafkaRule.getKafkaTestUtils().getAdminClient().deleteTopics(kafkaRule.getKafkaTestUtils().getTopicNames());
        producer = new MessageProducer();
        kafkaRule.getKafkaTestUtils().createTopic(PRODUCER_TEST_TOPIC, 1, (short) 1);
    }

    @Test
    @LocalData
    public void sendMessage() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("message", "success!");

        producer.sendMessage(PRODUCER_TEST_TOPIC, payload.toString());

        List<ConsumerRecord<String, String>> messages = kafkaRule.getKafkaTestUtils().consumeAllRecordsFromTopic(PRODUCER_TEST_TOPIC, StringDeserializer.class, StringDeserializer.class);

        assertEquals(messages.size(), 1);

        JSONObject receivedPayload = JSONObject.fromObject(messages.get(0).value());
        assertEquals(receivedPayload.getString("message"), "success!");
    }

    @After
    public void after() {
        //kafkaRule.getKafkaTestUtils().deleteTopic(PRODUCER_TEST_TOPIC);
    }
}