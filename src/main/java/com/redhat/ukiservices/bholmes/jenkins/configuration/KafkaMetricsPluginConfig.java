package com.redhat.ukiservices.bholmes.jenkins.configuration;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Properties;

@Extension
public class KafkaMetricsPluginConfig extends GlobalConfiguration {

    private static final String KEY_SERIALIZER = "org.apache.kafka.common.serialization.ByteArraySerializer";
    private static final String VALUE_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    /**
     * A comma separated list of bootstrap servers (hostname:port)
     */
    private String bootstrapServers;
    /**
     * A string id to pass to the Kafka server when making requests in order to be able then to track the source of requests.
     */
    private String clientId;

    /**
     * The topic of kafka to send the metrics messages to
     */
    private String metricsTopic;

    /**
     * The topic of kafka to send the log messages to
     */
    private String logTopic;

    /**
     * Default Constructor
     */
    public KafkaMetricsPluginConfig() {
        load();
    }

    /**
     * Get the current Kafka Global Configuration
     *
     * @return the Kafka Configuration, or {@code null} if Jenkins has been shut down
     */
    public static KafkaMetricsPluginConfig get() {
        Jenkins j = Jenkins.getInstance();
        KafkaMetricsPluginConfig conf = j.getDescriptorByType(KafkaMetricsPluginConfig.class);
        if (conf != null) {
            return conf;
        }
        return null;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getMetricsTopic() {
        return metricsTopic;
    }

    public void setMetricsTopic(String metricsTopic) {
        this.metricsTopic = metricsTopic;
    }

    public String getLogTopic() {
        return logTopic;
    }

    public void setLogTopic(String logTopic) {
        this.logTopic = logTopic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        return true;
    }

    /**
     * @return {@code java.util.Properties} object
     */
    public Properties getProperties() throws RuntimeException {
        Properties properties = new Properties();

        try {
            properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER);
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VALUE_SERIALIZER);
        } catch (NullPointerException npe) {
            throw new RuntimeException(npe);
        }

        return properties;
    }
}
