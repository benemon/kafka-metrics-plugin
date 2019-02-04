package com.redhat.ukiservices.jenkins.kafka;

import com.redhat.ukiservices.jenkins.kafka.common.CommonConstants;
import com.redhat.ukiservices.jenkins.kafka.common.PayloadType;
import com.redhat.ukiservices.jenkins.kafka.configuration.KafkaMetricsPluginConfig;
import com.redhat.ukiservices.jenkins.kafka.producer.MessageProducer;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import java.util.Optional;

public interface DefaultKafkaMetricsListener {

    default void sendMessage(JSONObject payload) {
        String topic = KafkaMetricsPluginConfig.get().getMetricsTopic();
        if ((null != topic) && (topic.length() > 0)) {
            try (MessageProducer producer = new MessageProducer()) {
                producer.sendMessage(topic, payload.toString());
            }
        }
    }

    default JSONObject createMetadata(PayloadType type) {
        JSONObject metadata = new JSONObject();
        metadata.put(CommonConstants.EVENT_TYPE_KEY, type);
        metadata.put(CommonConstants.ENVIRONMENT, this.processEnvironment());
        return metadata;
    }

    default JSONObject processEnvironment() {
        Optional<Jenkins> jenkins = Optional.ofNullable(Jenkins.getInstanceOrNull());
        JSONObject environment = new JSONObject();
        environment.put("version", Jenkins.VERSION);

        String jenkinsUrl = jenkins.get().getInstance().getRootUrl();
        environment.put("jenkinsUrl", jenkinsUrl);

        String project = System.getenv(CommonConstants.PROJECT_NAME_ENV) != null ? System.getenv(CommonConstants.PROJECT_NAME_ENV) : System.getenv(CommonConstants.KUBERNETES_NAMESPACE_ENV);
        environment.put("namespace", project);

        String hostName = System.getenv(CommonConstants.HOSTNAME_ENV);
        environment.put("hostName", hostName);

        return environment;
    }
}
