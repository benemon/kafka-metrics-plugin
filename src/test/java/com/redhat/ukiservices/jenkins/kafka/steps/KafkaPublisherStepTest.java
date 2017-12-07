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

import info.batey.kafka.unit.KafkaUnitRule;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.RestartableJenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.List;

import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;
import static org.junit.Assert.assertNotNull;

public class KafkaPublisherStepTest {

    public static final String PUBLISH_TEST_TOPIC = "publish-test";

    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @Rule
    public KafkaUnitRule kafkaRule = new KafkaUnitRule(5000, 5001);

    @Test
    @LocalData
    public void testPublishStepCustomTopic() throws Exception {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(new CpsFlowDefinition("kafkaPublisher(topic: 'publish-test', payload: [message: 'content'])", false));

                WorkflowRun build = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0));

                List<String> messages = kafkaRule.getKafkaUnit().readMessages(PUBLISH_TEST_TOPIC, 1);

                assertEquals(1, messages.size());

                JSONObject obj = JSONObject.fromObject(messages.get(0));

                assertNotNull(obj);

                assertEquals("content", obj.get("message"));
            }
        });

    }

    @Test
    @LocalData
    public void testPublishStepDefaultTopic() throws Exception {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(new CpsFlowDefinition("kafkaPublisher(payload: [message: 'content'])", false));

                WorkflowRun build = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0));

                List<String> messages = kafkaRule.getKafkaUnit().readMessages("logs", 5);

                assertEquals(5, messages.size());

                JSONObject obj = JSONObject.fromObject(messages.get(0));

                assertNotNull(obj);

                assertEquals("content", obj.get("message"));
            }
        });

    }

    @Test
    @LocalData
    public void testPublishStepParallel() throws Exception {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(
                        new CpsFlowDefinition("parallel ( 'customPublish': { kafkaPublisher(topic: 'publish-test', payload: [message: 'customPublishContent']) }, 'defaultPublish' : { kafkaPublisher( payload: [message: 'defaultPublishContent'] ) } )", false));

                WorkflowRun build = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0));

                List<String> customMessages = kafkaRule.getKafkaUnit().readMessages(PUBLISH_TEST_TOPIC, 1);

                assertEquals(1, customMessages.size());

                JSONObject cObj = JSONObject.fromObject(customMessages.get(0));

                assertNotNull(cObj);

                assertEquals("customPublishContent", cObj.get("message"));

                List<String> defaultMessages = kafkaRule.getKafkaUnit().readMessages("logs", 12);

                assertEquals(12, defaultMessages.size());

                JSONObject dObj = JSONObject.fromObject(defaultMessages.get(0));

                assertNotNull(dObj);

                assertEquals("defaultPublishContent", dObj.get("message"));
            }
        });

    }


}
