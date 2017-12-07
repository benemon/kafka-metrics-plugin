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
        JSONArray dockerClouds = receivedPayload.getJSONObject("clouds").getJSONArray("docker");
        assertNotNull(dockerClouds);
        JSONObject dockerCloud = (JSONObject) dockerClouds.get(0);
        assertNotNull(dockerCloud);
        assertEquals("docker swarm", dockerCloud.get("name"));
        JSONArray templates = (JSONArray) dockerCloud.get("templates");
        assertEquals(2, templates.size());
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