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

import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import java.util.logging.Logger;

public abstract class AbstractKafkaMetricsPluginRunListener extends RunListener<Run> {

    private static final Logger log = Logger.getLogger(AbstractKafkaMetricsPluginRunListener.class.getName());

    private static final String PROJECT_NAME_ENV = "PROJECT_NAME";
    private static final String KUBERNETES_NAMESPACE_ENV = "KUBERNETES_NAMESPACE";
    private static final String JENKINS_URL_ENV = "JENKINS_URL";
    private static final String JOB_NAME_ENV = "JOB_NAME";

    /**
     * Wrapper for processEnvironment for the use case where no current TaskListener exist
     *
     * @param run the Run object
     * @return JSONObject
     */
    protected JSONObject processEnvironment(Run run) {

        return processEnvironment(run, null);
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
        environment.put("jenkinsUrl", listener != null ? getJenkinsUrl(run, listener) : System.getenv(JENKINS_URL_ENV));
        environment.put("job", listener != null ? getJenkinsJobName(run, listener) : System.getenv(JOB_NAME_ENV));
        environment.put("namespace", this.getOpenShiftNamespace());

        return environment;
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
            url = run.getEnvironment(listener).get(JENKINS_URL_ENV);
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
        String project = System.getenv(PROJECT_NAME_ENV) != null ? System.getenv(PROJECT_NAME_ENV) : System.getenv(KUBERNETES_NAMESPACE_ENV);

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
            job = run.getEnvironment(listener).get(JOB_NAME_ENV);

            // last ditch attempt to get something valid
            if (job == null) {
                job = run.getFullDisplayName().substring(0, run.getFullDisplayName().lastIndexOf("#") - 1);
            }

        } catch (Exception e) {
            log.warning("Could not retrieve Job Name");
        }

        return job;
    }
}
