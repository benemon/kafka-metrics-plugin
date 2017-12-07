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

package com.redhat.ukiservices.jenkins.kafka.job.events;

import com.redhat.ukiservices.jenkins.kafka.configuration.KafkaMetricsPluginConfig;
import com.redhat.ukiservices.jenkins.kafka.job.base.AbstractKafkaMetricsPluginRunListener;
import com.redhat.ukiservices.jenkins.kafka.producer.MessageProducer;
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

