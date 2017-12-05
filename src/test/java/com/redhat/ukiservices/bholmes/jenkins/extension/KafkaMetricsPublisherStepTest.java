package com.redhat.ukiservices.bholmes.jenkins.extension;

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

public class KafkaMetricsPublisherStepTest {

    public static final String PUBLISH_TEST_TOPIC = "publish-test";

    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @Rule
    public KafkaUnitRule kafkaRule = new KafkaUnitRule(5000, 5001);

    @Test
    @LocalData
    public void testMetricsStep() throws Exception {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(new CpsFlowDefinition("kafkaMetricsPublisher(topic: 'publish-test', payload: [message: 'content'])", false));

                WorkflowRun build = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0));

                List<String> messages = kafkaRule.getKafkaUnit().readMessages(PUBLISH_TEST_TOPIC, 1);

                assertEquals(1, messages.size());

                JSONObject obj = JSONObject.fromObject(messages.get(0));

                assertNotNull(obj);

                assertEquals("content", obj.get("message"));
            }
        });

    }


}
