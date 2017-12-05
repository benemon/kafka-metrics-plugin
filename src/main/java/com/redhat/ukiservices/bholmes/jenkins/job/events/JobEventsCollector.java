package com.redhat.ukiservices.bholmes.jenkins.job.events;

import com.redhat.ukiservices.bholmes.jenkins.configuration.KafkaMetricsPluginConfig;
import com.redhat.ukiservices.bholmes.jenkins.producer.MessageProducer;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

@Extension
public class JobEventsCollector extends RunListener<Run> {

    private static final String PROJECT_NAME_ENV = "PROJECT_NAME";
    private static final String KUBERNETES_NAMESPACE_ENV = "KUBERNETES_NAMESPACE";
    private static final String JENKINS_URL_ENV = "JENKINS_URL";

    private static final Logger log = Logger.getLogger(JobEventsCollector.class.getName());

    @Override
    public void onStarted(Run run, TaskListener listener) {
        super.onStarted(run, listener);

        new MessageProducer().sendMessage(KafkaMetricsPluginConfig.get().getTopic(), generateStartedPayload(run, listener).toString());
    }

    @Override
    public void onCompleted(Run run, @Nonnull TaskListener listener) {
        super.onCompleted(run, listener);

        new MessageProducer().sendMessage(KafkaMetricsPluginConfig.get().getTopic(), generateCompletedPayload(run, listener).toString());
    }


    private JSONObject generateStartedPayload(Run run, TaskListener listener) {
        JSONObject startedPayload = new JSONObject();

        startedPayload.put("name", run.getDisplayName());
        startedPayload.put("buildNumber", run.getNumber());
        startedPayload.put("project", this.getProjectName());
        startedPayload.put("jenkinsUrl", this.getJenkinsUrl(run, listener));
        startedPayload.put("estimatedDuration", run.getEstimatedDuration());

        return startedPayload;
    }

    private JSONObject generateCompletedPayload(Run run, TaskListener listener) {
        JSONObject completedPayload = new JSONObject();

        completedPayload.put("name", run.getDisplayName());
        completedPayload.put("buildNumber", run.getNumber());
        completedPayload.put("project", this.getProjectName());
        completedPayload.put("jenkinsUrl", this.getJenkinsUrl(run, listener));
        completedPayload.put("estimatedDuration", run.getEstimatedDuration());
        completedPayload.put("actualDuration", run.getDuration());
        completedPayload.put("result", run.getResult().toString());

        return completedPayload;
    }

    /**
     * Get Jenkins URL
     *
     * @param run
     * @param listener
     * @return
     */
    private String getJenkinsUrl(Run run, TaskListener listener) {
        String url = null;
        try {
            url = run.getEnvironment(listener).get(JENKINS_URL_ENV);
        } catch (Exception e) {
            log.warning("Could not retrieve Jenkins URL");
        }

        return url;
    }

    /**
     * Get Project Name
     *
     * @return String
     */
    private String getProjectName() {
        String project = System.getenv(PROJECT_NAME_ENV) != null ? System.getenv(PROJECT_NAME_ENV) : System.getenv(KUBERNETES_NAMESPACE_ENV);

        return project;
    }
}

