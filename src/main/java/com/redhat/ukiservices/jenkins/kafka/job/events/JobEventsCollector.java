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

import com.redhat.ukiservices.jenkins.kafka.common.CommonConstants;
import com.redhat.ukiservices.jenkins.kafka.common.PayloadType;
import com.redhat.ukiservices.jenkins.kafka.job.base.AbstractKafkaMetricsPluginRunListener;
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
        JSONObject msg = generateStartedPayload(run, listener);
        sendMetricsMessage(msg);
    }

    @Override
    public void onCompleted(Run run, @Nonnull TaskListener listener) {
        JSONObject msg = generateCompletedPayload(run, listener);
        sendMetricsMessage(msg);
    }


    private JSONObject generateStartedPayload(Run run, TaskListener listener) {
        JSONObject startedPayload = generateBasePayload(run, listener);
        JSONObject job = startedPayload.getJSONObject(CommonConstants.DATA).getJSONObject(CommonConstants.JOB);
        job.put(CommonConstants.ESTIMATED_DURATION, run.getEstimatedDuration());
        return startedPayload;
    }

    private JSONObject generateCompletedPayload(Run run, TaskListener listener) {
        JSONObject completedPayload = generateBasePayload(run, listener);
        JSONObject job = completedPayload.getJSONObject(CommonConstants.DATA).getJSONObject(CommonConstants.JOB);
        job.put(CommonConstants.ESTIMATED_DURATION, run.getEstimatedDuration());
        job.put(CommonConstants.ACTUAL_DURATION, run.getDuration());
        job.put(CommonConstants.RESULT, run.getResult().toString());
        return completedPayload;
    }

    private JSONObject generateBasePayload(Run run, TaskListener listener) {
        JSONObject payload = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject job = new JSONObject();
        job.put(CommonConstants.JOB_NAME, this.getJenkinsJobName(run, listener));
        data.put(CommonConstants.JOB, job);
        payload.put(CommonConstants.METADATA, createMetadata(PayloadType.JOB, run, listener));
        payload.put(CommonConstants.DATA, data);
        return payload;
    }
}

