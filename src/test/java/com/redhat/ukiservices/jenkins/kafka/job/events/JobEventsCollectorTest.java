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

package com.redhat.ukiservices.jenkins.kafka.job.events;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
import hudson.model.Result;
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

import static junit.framework.Assert.assertEquals;

public class JobEventsCollectorTest {

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
    public void onStartedTest() {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(new CpsFlowDefinition("echo 'test'", false));

                WorkflowRun build = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0));

                List<ConsumerRecord<String, String>> messages = kafkaRule.getKafkaTestUtils().consumeAllRecordsFromTopic("metrics", StringDeserializer.class, StringDeserializer.class);

                assertEquals(3, messages.size());

                JSONObject buildStarted = JSONObject.fromObject(messages.get(1).value());
            }
        });
    }

    @Test
    @LocalData
    public void onCompletedTestSuccess() {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(new CpsFlowDefinition("echo 'test'", false));

                WorkflowRun build = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0));

                List<ConsumerRecord<String, String>> messages = kafkaRule.getKafkaTestUtils().consumeAllRecordsFromTopic("metrics", StringDeserializer.class, StringDeserializer.class);

                assertEquals(3, messages.size());

                JSONObject buildCompleted = JSONObject.fromObject(messages.get(2).value());

                assertEquals(Result.SUCCESS.toString(), buildCompleted.getJSONObject("data").getJSONObject("job").get("result"));
            }
        });
    }

    @Test
    @LocalData
    public void onCompletedTestFailure() {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(new CpsFlowDefinition("echo i am a bad job", false));

                WorkflowRun build = story.j.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0));

                List<ConsumerRecord<String, String>> messages = kafkaRule.getKafkaTestUtils().consumeAllRecordsFromTopic("metrics", StringDeserializer.class, StringDeserializer.class);

                assertEquals(3, messages.size());

                JSONObject buildCompleted = JSONObject.fromObject(messages.get(2).value());

                assertEquals(Result.FAILURE.toString(), buildCompleted.getJSONObject("data").getJSONObject("job").get("result"));
            }
        });
    }

}