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

import info.batey.kafka.unit.KafkaUnitRule;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class MessageProducerTest {

    public static final String PRODUCER_TEST_TOPIC = "producer-test";
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Rule
    public KafkaUnitRule kafkaRule = new KafkaUnitRule(5000, 5001);

    private MessageProducer producer;

    @Before
    public void before() throws Exception {
        producer = new MessageProducer();
        kafkaRule.getKafkaUnit().createTopic(PRODUCER_TEST_TOPIC);
    }

    @Test
    @LocalData
    public void sendMessage() throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("message", "success!");

        producer.sendMessage(PRODUCER_TEST_TOPIC, payload.toString());

        List<String> messages = kafkaRule.getKafkaUnit().readMessages(PRODUCER_TEST_TOPIC, 1);
        assertEquals(messages.size(), 1);
        JSONObject receivedPayload = JSONObject.fromObject(messages.get(0));
        assertEquals(receivedPayload.getString("message"), "success!");
    }

    @After
    public void after() throws Exception {
        kafkaRule.getKafkaUnit().deleteTopic(PRODUCER_TEST_TOPIC);
    }
}