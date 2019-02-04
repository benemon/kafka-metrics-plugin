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

package com.redhat.ukiservices.jenkins.kafka.job.base;

import com.redhat.ukiservices.jenkins.kafka.common.CommonConstants;
import com.redhat.ukiservices.jenkins.kafka.common.PayloadType;
import com.redhat.ukiservices.jenkins.kafka.configuration.KafkaMetricsPluginConfig;
import com.redhat.ukiservices.jenkins.kafka.producer.MessageProducer;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.util.LogTaskListener;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractKafkaMetricsPluginRunListener extends RunListener<Run> {

    private static final Logger log = Logger.getLogger(AbstractKafkaMetricsPluginRunListener.class.getName());


    /**
     * Convenience method for sending metrics messages
     *
     * @param msg
     */
    protected void sendMetricsMessage(JSONObject msg) {
        String topic = KafkaMetricsPluginConfig.get().getMetricsTopic();
        if ((null != topic) && (topic.length() > 0)) {
            sendMessage(msg, topic);
        } else {
            log.warning("Metrics Topic not configured.");
        }
    }

    /**
     * Convenience method for sending log messages
     *
     * @param msg
     */
    protected void sendLogMessage(JSONObject msg) {
        String topic = KafkaMetricsPluginConfig.get().getLogTopic();
        if ((null != topic) && (topic.length() > 0)) {
            sendMessage(msg, topic);
        } else {
            log.warning("Logs Topic not configured.");
        }
    }

    /**
     * Send a JSONObject to the specified topic
     *
     * @param msg
     * @param topic
     */
    protected void sendMessage(JSONObject msg, String topic) {
        if ((null != topic) && (topic.length() > 0)) {
            try (MessageProducer producer = new MessageProducer()) {
                producer.sendMessage(topic, msg.toString());
            }
        } else {
            log.warning("Topic not configured.");
        }
    }

    /**
     * Wrapper for processEnvironment for the use case where no current TaskListener exist
     *
     * @param run the Run object
     * @return JSONObject
     */
    protected JSONObject processEnvironment(Run run) {
        return processEnvironment(run, new LogTaskListener(log, Level.INFO));
    }


    /**
     * Get a formatted object containing metadata for the payload
     *
     * @return
     */
    protected JSONObject createMetadata(PayloadType type, Run run, TaskListener listener) {
        JSONObject metadata = new JSONObject();
        metadata.put(CommonConstants.EVENT_TYPE_KEY, type);
        metadata.put(CommonConstants.ENVIRONMENT, this.processEnvironment(run, listener));
        return metadata;
    }


    /**
     * Get a standard set of environment information in a clean format
     *
     * @param run      the Run object
     * @param listener the Listener object
     * @return JSONObject
     */
    protected JSONObject processEnvironment(Run run, TaskListener listener) {
        JSONObject environment = new JSONObject();
        environment.put("version", Jenkins.VERSION);
        environment.put("jenkinsUrl", this.getJenkinsUrl(run, listener));
        environment.put("namespace", this.getOpenShiftNamespace());
        environment.put("hostname", this.getHostname(run, listener));
        return environment;
    }

    /**
     * Get Hostname
     *
     * @param run      the Run object
     * @param listener the Listener object
     * @return the Hostname
     */
    protected String getHostname(Run run, TaskListener listener) {
        String url = null;
        try {
            url = run.getEnvironment(listener).get(CommonConstants.HOSTNAME_ENV);

            // last ditch attempt to get something valid
            if (url == null) {
                url = System.getenv(CommonConstants.HOSTNAME_ENV);
            }
        } catch (Exception e) {
            log.warning("Could not retrieve Hostname");
        }

        return url;
    }

    /**
     * Get Jenkins URL
     *
     * @param run      the Run object
     * @param listener the Listener object
     * @return the Jenkins URL
     */
    protected String getJenkinsUrl(Run run, TaskListener listener) {
        String url = null;
        try {
            url = run.getEnvironment(listener).get(CommonConstants.JENKINS_URL_ENV);

            // last ditch attempt to get something valid
            if (url == null) {
                url = System.getenv(CommonConstants.JENKINS_URL_ENV);
            }
        } catch (Exception e) {
            log.warning("Could not retrieve Jenkins URL");
        }

        return url;
    }

    /**
     * Get OpenShift Project Name
     *
     * @return String
     */
    protected String getOpenShiftNamespace() {
        String project = System.getenv(CommonConstants.PROJECT_NAME_ENV) != null ? System.getenv(CommonConstants.PROJECT_NAME_ENV) : System.getenv(CommonConstants.KUBERNETES_NAMESPACE_ENV);

        return project;
    }

    /**
     * Get Jenkins Job Name
     *
     * @param run      the Run object
     * @param listener the Listener object
     * @return String
     */
    protected String getJenkinsJobName(Run run, TaskListener listener) {
        String job = null;
        try {
            job = run.getEnvironment(listener).get(CommonConstants.JOB_NAME_ENV);

            // last ditch attempt to get something valid
            if (job == null) {
                job = run.getFullDisplayName().substring(0, run.getFullDisplayName().lastIndexOf("#") - 1);

                // sigh
                if (job == null) {
                    job = System.getenv(CommonConstants.JOB_NAME_ENV);
                }
            }

        } catch (Exception e) {
            log.warning("Could not retrieve Job Name");
        }

        return job;
    }
}
