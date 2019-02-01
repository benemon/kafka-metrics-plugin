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

package com.redhat.ukiservices.jenkins.kafka.job.logging;

import com.salesforce.kafka.test.junit4.SharedKafkaTestResource;
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

public class JobLogCollectorTest {

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
    public void onFinalizedTest() {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(new CpsFlowDefinition(createBuildJob(), false));

                WorkflowRun build = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0));

                List<ConsumerRecord<String, String>> messages = kafkaRule.getKafkaTestUtils().consumeAllRecordsFromTopic("logs", StringDeserializer.class, StringDeserializer.class);

            }
        });
    }

    /**
     * Create a example build job
     *
     * @return
     */
    private String createBuildJob() {
        StringBuilder builder = new StringBuilder();

        builder.append("node { ");
        builder.append(System.lineSeparator());
        builder.append("  stage \"Stage Checkout\" ");
        builder.append(System.lineSeparator());
        builder.append("    echo \"I've checked some stuff out\" ");
        builder.append(System.lineSeparator());
        builder.append("  stage \"Stage Build\" ");
        builder.append(System.lineSeparator());
        builder.append("    echo \"Building the thing\" ");
        builder.append(System.lineSeparator());
        builder.append("  stage \"Stage Archive\" ");
        builder.append(System.lineSeparator());
        builder.append("    echo \"Archiving the thing\" ");
        builder.append(System.lineSeparator());
        builder.append("  stage \"Stage Deploy\" ");
        builder.append(System.lineSeparator());
        builder.append("    echo \"Deploying the thing\" ");
        builder.append(System.lineSeparator());
        builder.append("}");

        return builder.toString();
    }
}