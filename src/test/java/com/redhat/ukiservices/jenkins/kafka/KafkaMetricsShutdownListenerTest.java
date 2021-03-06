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

package com.redhat.ukiservices.jenkins.kafka;

import com.redhat.ukiservices.jenkins.kafka.common.PayloadType;
import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class KafkaMetricsShutdownListenerTest {

    @ClassRule
    public static final SharedKafkaTestResource kafkaRule = new SharedKafkaTestResource()
            .withBrokerProperty("port", "5001")
            .withBrokerProperty("host.name", "localhost");
    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    @Ignore
    public void onShutdown() throws Exception {
        List<ConsumerRecord<String, String>> messages = kafkaRule.getKafkaTestUtils().consumeAllRecordsFromTopic("metrics", StringDeserializer.class, StringDeserializer.class);
        assertEquals(2, messages.size());
        JSONObject receivedPayload = JSONObject.fromObject(messages.get(1).value());
        System.out.println(receivedPayload);
        assertEquals(Jenkins.VERSION, receivedPayload.getJSONObject("metadata").getJSONObject("environment").getString("version"));
        assertEquals(PayloadType.DEREGISTER, PayloadType.valueOf(receivedPayload.getJSONObject("metadata").getString("type")));
    }

}