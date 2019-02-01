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

package com.redhat.ukiservices.jenkins.kafka.configuration;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Optional;
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
        Optional<Jenkins> jenkins = Optional.ofNullable(Jenkins.getInstanceOrNull());

        if (jenkins.isPresent()) {
            KafkaMetricsPluginConfig conf = jenkins.get().getDescriptorByType(KafkaMetricsPluginConfig.class);
            if (conf != null) {
                return conf;
            }
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
    public boolean configure(StaplerRequest req, JSONObject json) {
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
