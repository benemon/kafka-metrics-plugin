package com.redhat.ukiservices.bholmes.jenkins.job.events;

import hudson.model.Result;
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

import static junit.framework.Assert.assertEquals;

public class JobEventsCollectorTest {

    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @Rule
    public KafkaUnitRule kafkaRule = new KafkaUnitRule(5000, 5001);

    @Test
    @LocalData
    public void onStartedTest() throws Exception {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(new CpsFlowDefinition("echo 'test'", false));

                WorkflowRun build = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0));

                List<String> messages = kafkaRule.getKafkaUnit().readMessages("metrics", 3);

                assertEquals(3, messages.size());

                JSONObject buildStarted = JSONObject.fromObject(messages.get(1));
            }
        });
    }

    @Test
    @LocalData
    public void onCompletedTestSuccess() throws Exception {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(new CpsFlowDefinition("echo 'test'", false));

                WorkflowRun build = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0));

                List<String> messages = kafkaRule.getKafkaUnit().readMessages("metrics", 3);

                assertEquals(3, messages.size());

                JSONObject buildCompleted = JSONObject.fromObject(messages.get(2));

                assertEquals(Result.SUCCESS.toString(), buildCompleted.get("result"));
            }
        });
    }

    @Test
    @LocalData
    public void onCompletedTestFailure() throws Exception {

        story.addStep(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "randomName" + System.currentTimeMillis());

                job.setDefinition(new CpsFlowDefinition("echo i am a bad job", false));

                WorkflowRun build = story.j.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0));

                List<String> messages = kafkaRule.getKafkaUnit().readMessages("metrics", 3);

                assertEquals(3, messages.size());

                JSONObject buildCompleted = JSONObject.fromObject(messages.get(2));

                assertEquals(Result.FAILURE.toString(), buildCompleted.get("result"));
            }
        });
    }

}