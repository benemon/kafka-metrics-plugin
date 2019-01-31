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

package com.redhat.ukiservices.jenkins.kafka.steps;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import net.sf.json.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.RestartableJenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.ArrayList;
import java.util.List;

import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;
import static org.junit.Assert.assertNotNull;

public class KafkaPublisherStepTest {

    public static final String PUBLISH_TEST_TOPIC = "publish-test";

    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @ClassRule
    public static final SharedKafkaTestResource kafkaRule = new SharedKafkaTestResource()
            .withBrokerProperty("port", "5001")
            .withBrokerProperty("host.name", "localhost");



    @Before
    public void beforeTest() throws Exception {
        kafkaRule.getKafkaTestUtils().getAdminClient().deleteTopics(kafkaRule.getKafkaTestUtils().getTopicNames());
    }

    @Test
    @LocalData
    public void testPublishStepCustomTopic() {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(new CpsFlowDefinition("kafkaPublisher(topic: 'publish-test', payload: [message: 'content'])", false));

                WorkflowRun build = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0));

                List<ConsumerRecord<String, String>> messages = kafkaRule.getKafkaTestUtils().consumeAllRecordsFromTopic(PUBLISH_TEST_TOPIC, StringDeserializer.class, StringDeserializer.class);

                assertEquals(1, messages.size());

                JSONObject obj = JSONObject.fromObject(messages.get(0).value());

                assertNotNull(obj);

                assertEquals("content", obj.get("message"));
            }
        });

    }

    @Test
    @LocalData
    public void testPublishStepDefaultTopic() {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(new CpsFlowDefinition("kafkaPublisher(payload: [message: 'content'])", false));

                WorkflowRun build = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0));

                List<ConsumerRecord<String, String>> messages = kafkaRule.getKafkaTestUtils().consumeAllRecordsFromTopic("logs", StringDeserializer.class, StringDeserializer.class);

                assertEquals(5, messages.size());

                JSONObject obj = JSONObject.fromObject(messages.get(0).value());

                assertNotNull(obj);

                assertEquals("content", obj.get("message"));
            }
        });

    }

    @Test
    @LocalData
    public void testPublishStepParallel() {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(
                        new CpsFlowDefinition("parallel ( 'customPublish': { kafkaPublisher(topic: 'publish-test', payload: [message: 'customPublishContent']) }, 'defaultPublish' : { kafkaPublisher( payload: [message: 'defaultPublishContent'] ) } )", false));

                WorkflowRun build = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0));

                List<ConsumerRecord<String, String>> customMessages = kafkaRule.getKafkaTestUtils().consumeAllRecordsFromTopic(PUBLISH_TEST_TOPIC, StringDeserializer.class, StringDeserializer.class);

                assertEquals(1, customMessages.size());

                JSONObject cObj = JSONObject.fromObject(customMessages.get(0).value());

                assertNotNull(cObj);

                assertEquals("customPublishContent", cObj.get("message"));

                List<ConsumerRecord<String, String>> defaultMessages = kafkaRule.getKafkaTestUtils().consumeAllRecordsFromTopic("logs", StringDeserializer.class, StringDeserializer.class);

                assertEquals(12, defaultMessages.size());

                JSONObject dObj = JSONObject.fromObject(defaultMessages.get(0).value());

                assertNotNull(dObj);

                assertEquals("defaultPublishContent", dObj.get("message"));
            }
        });

    }


}
