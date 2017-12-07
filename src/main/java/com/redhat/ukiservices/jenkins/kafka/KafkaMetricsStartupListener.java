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

import com.redhat.ukiservices.jenkins.kafka.configuration.KafkaMetricsPluginConfig;
import com.redhat.ukiservices.jenkins.kafka.optionals.DockerCloudInfo;
import com.redhat.ukiservices.jenkins.kafka.optionals.KubernetesCloudInfo;
import com.redhat.ukiservices.jenkins.kafka.producer.MessageProducer;
import hudson.PluginWrapper;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

public class KafkaMetricsStartupListener {

    private static final String PROJECT_NAME_ENV = "PROJECT_NAME";
    private static final String KUBERNETES_NAMESPACE_ENV = "KUBERNETES_NAMESPACE";

    @Initializer(after = InitMilestone.JOB_LOADED)
    public void onStartup() {
        JSONObject payload = new JSONObject();
        payload.put("environment", this.processEnvironment());
        payload.put("clouds", this.processClouds());
        payload.put("plugins", this.processPlugins());

        try (MessageProducer producer = new MessageProducer()) {
            producer.sendMessage(KafkaMetricsPluginConfig.get().getMetricsTopic(), payload.toString());
        }

    }

    private JSONObject processEnvironment() {
        JSONObject environment = new JSONObject();
        environment.put("version", Jenkins.VERSION);

        String project = System.getenv(PROJECT_NAME_ENV) != null ? System.getenv(PROJECT_NAME_ENV) : System.getenv(KUBERNETES_NAMESPACE_ENV);
        environment.put("project", project);

        return environment;
    }

    private JSONObject processClouds() {
        JSONObject clouds = new JSONObject();

        Jenkins.CloudList cloudlist = Jenkins.getInstance().clouds;

        if (Jenkins.getInstance().getPlugin("kubernetes") != null) {
            JSONArray kubeClouds = new KubernetesCloudInfo().getKubeClouds();
            clouds.put("kubernetes", kubeClouds);
        }

        if (Jenkins.getInstance().getPlugin("docker-plugin") != null) {
            JSONArray dockerClouds = new DockerCloudInfo().getDockerClouds();
            clouds.put("docker", dockerClouds);
        }

        return clouds;
    }


    private JSONArray processPlugins() {
        JSONArray plugins = new JSONArray();

        List<PluginWrapper> pluginList = Jenkins.getInstance().pluginManager.getPlugins();

        for (PluginWrapper plugin : pluginList) {
            JSONObject pluginJson = new JSONObject();
            pluginJson.put("longName", plugin.getLongName());
            pluginJson.put("displayName", plugin.getDisplayName());
            pluginJson.put("shortName", plugin.getShortName());
            pluginJson.put("version", plugin.getVersion());

            plugins.add(pluginJson);
        }

        return plugins;
    }

}
