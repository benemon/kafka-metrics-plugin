package com.redhat.ukiservices.jenkins.kafka.steps;

import com.redhat.ukiservices.jenkins.kafka.configuration.KafkaMetricsPluginConfig;
import com.redhat.ukiservices.jenkins.kafka.producer.MessageProducer;
import hudson.Extension;
import hudson.model.TaskListener;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class KafkaPublisherStep extends Step {

    private static Logger log = Logger.getLogger(KafkaPublisherStep.class.getName());

    private final String topic;

    private final Map<String, String> payload;

    @DataBoundConstructor
    public KafkaPublisherStep(String topic, Map<String, String> payload) {
        this.payload = payload;
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    @Override
    public StepExecution start(StepContext stepContext) {
        return new Execution(topic, payload, stepContext);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Collections.singleton(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "kafkaPublisher";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Kafka Publisher";
        }
    }

    public static class Execution extends SynchronousStepExecution<Void> {

        private static final long serialVersionUID = 1L;
        private transient final String topic;
        private transient final Map<String, String> payload;

        Execution(String topic, Map<String, String> payload, StepContext context) {
            super(context);

            this.topic = topic;
            this.payload = payload;
        }

        @Override
        protected Void run() {

            JSONObject publishedContent = new JSONObject();
            publishedContent.putAll(payload);
            String localTopic = topic != null ? topic : KafkaMetricsPluginConfig.get().getLogTopic();

            if (null != localTopic && localTopic.length() > 0) {
                try (MessageProducer producer = new MessageProducer()) {
                    producer.sendMessage(localTopic, publishedContent.toString());
                }
            } else {
                log.warning("Destination topic is not configured correctly in either the Pipeline step, or the System. Please check your configuration.");
            }


            return null;
        }

    }
}
