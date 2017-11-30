package com.redhat.ukiservices.bholmes.jenkins;

import com.redhat.ukiservices.bholmes.jenkins.configuration.KafkaMetricsPluginConfig;
import com.redhat.ukiservices.bholmes.jenkins.optionals.DockerCloudInfo;
import com.redhat.ukiservices.bholmes.jenkins.optionals.KubernetesCloudInfo;
import com.redhat.ukiservices.bholmes.jenkins.producer.MessageProducer;
import hudson.Plugin;
import hudson.PluginWrapper;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.slaves.Cloud;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

public class KafkaMetricsStartupListener extends Plugin {

    private static final String PROJECT_NAME_ENV = "PROJECT_NAME";
    private static final String KUBERNETES_NAMESPACE_ENV = "KUBERNETES_NAMESPACE";

    @Initializer(after = InitMilestone.JOB_LOADED)
    public void onStartup() {
        JSONObject payload = new JSONObject();
        payload.put("environment", this.processEnvironment());
        payload.put("clouds", this.processClouds());
        payload.put("plugins", this.processPlugins());

        new MessageProducer().sendMessage(KafkaMetricsPluginConfig.get().getTopic(), payload.toString());
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
        }

        if (Jenkins.getInstance().getPlugin("docker") != null) {
            JSONArray dockerClouds = new DockerCloudInfo().getDockerClouds();
        }

        for (Cloud cloud : cloudlist) {
            JSONObject cloudJson = new JSONObject();
            cloudJson.put("name", cloud.name);
            cloudJson.put("displayName", cloud.getDisplayName());
            clouds.put(cloud.name, cloudJson);
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
