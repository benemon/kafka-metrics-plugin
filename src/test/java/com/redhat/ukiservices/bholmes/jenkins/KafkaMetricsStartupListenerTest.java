package com.redhat.ukiservices.bholmes.jenkins;

import info.batey.kafka.unit.KafkaUnitRule;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import java.util.List;

import static org.junit.Assert.assertEquals;

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
        System.out.println(receivedPayload.toString());
        assertEquals(Jenkins.VERSION, receivedPayload.getJSONObject("environment").getString("version"));
    }
}