package com.redhat.ukiservices.bholmes.jenkins;

import info.batey.kafka.unit.KafkaUnitRule;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class KafkaMetricsStartupListenerTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Rule
    public KafkaUnitRule kafkaRule = new KafkaUnitRule(5000, 5001);


    @Test
    @LocalData
    public void onStartup() throws Exception {
        List<String> messages = kafkaRule.getKafkaUnit().readMessages("metrics", 1);
        assertEquals(messages.size(), 1);
        JSONObject receivedPayload = JSONObject.fromObject(messages.get(0));
        assertEquals(Jenkins.VERSION, receivedPayload.getJSONObject("environment").getString("version"));
    }


    @Test
    @LocalData
    public void dockerCloudConfigTest() throws Exception {
        List<String> messages = kafkaRule.getKafkaUnit().readMessages("metrics", 1);
        assertEquals(messages.size(), 1);
        JSONObject receivedPayload = JSONObject.fromObject(messages.get(0));
        assertNotNull(receivedPayload);
        JSONArray dockerCloud = (JSONArray) receivedPayload.getJSONObject("clouds").getJSONArray("docker").get(0);
        assertNotNull(dockerCloud);
        assertEquals(2, dockerCloud.size());
    }


    @Test
    @LocalData
    public void kubernetesCloudConfigTest() throws Exception {
        List<String> messages = kafkaRule.getKafkaUnit().readMessages("metrics", 1);
        assertEquals(messages.size(), 1);
        JSONObject receivedPayload = JSONObject.fromObject(messages.get(0));
        assertNotNull(receivedPayload);
        JSONArray kubernetes = receivedPayload.getJSONObject("clouds").getJSONArray("kubernetes");
        assertNotNull(kubernetes);
        JSONObject andreCloud = (JSONObject) kubernetes.get(0);
        assertNotNull(andreCloud);
        assertEquals("andre-kubernetes-cloud", andreCloud.get("name"));

    }
}