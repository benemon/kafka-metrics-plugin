package com.redhat.ukiservices.bholmes.jenkins.job.events;

import com.redhat.ukiservices.bholmes.jenkins.configuration.KafkaMetricsPluginConfig;
import com.redhat.ukiservices.bholmes.jenkins.job.base.AbstractKafkaMetricsPluginRunListener;
import com.redhat.ukiservices.bholmes.jenkins.producer.MessageProducer;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

@Extension
public class JobEventsCollector extends AbstractKafkaMetricsPluginRunListener {

    private static final Logger log = Logger.getLogger(JobEventsCollector.class.getName());

    @Override
    public void onStarted(Run run, TaskListener listener) {
        super.onStarted(run, listener);

        try (MessageProducer producer = new MessageProducer()) {
            producer.sendMessage(KafkaMetricsPluginConfig.get().getMetricsTopic(), generateStartedPayload(run, listener).toString());
        }

    }

    @Override
    public void onCompleted(Run run, @Nonnull TaskListener listener) {
        try (MessageProducer producer = new MessageProducer()) {
            producer.sendMessage(KafkaMetricsPluginConfig.get().getMetricsTopic(), generateCompletedPayload(run, listener).toString());
        }
    }


    private JSONObject generateStartedPayload(Run run, TaskListener listener) {
        JSONObject startedPayload = new JSONObject();

        startedPayload.put("environment", processEnvironment(run, listener));
        startedPayload.put("estimatedDuration", run.getEstimatedDuration());

        return startedPayload;
    }

    private JSONObject generateCompletedPayload(Run run, TaskListener listener) {
        JSONObject completedPayload = new JSONObject();

        completedPayload.put("environment", processEnvironment(run, listener));
        completedPayload.put("estimatedDuration", run.getEstimatedDuration());
        completedPayload.put("actualDuration", run.getDuration());
        completedPayload.put("result", run.getResult().toString());

        return completedPayload;
    }
}

