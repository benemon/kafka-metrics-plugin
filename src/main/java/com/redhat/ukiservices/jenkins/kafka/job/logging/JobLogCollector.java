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

package com.redhat.ukiservices.jenkins.kafka.job.logging;

import com.redhat.ukiservices.jenkins.kafka.configuration.KafkaMetricsPluginConfig;
import com.redhat.ukiservices.jenkins.kafka.job.base.AbstractKafkaMetricsPluginRunListener;
import com.redhat.ukiservices.jenkins.kafka.producer.MessageProducer;
import hudson.Extension;
import hudson.model.Run;
import hudson.util.LogTaskListener;
import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class JobLogCollector extends AbstractKafkaMetricsPluginRunListener {

    private static final Logger log = Logger.getLogger(JobLogCollector.class.getName());

    @Override
    public void onFinalized(Run run) {

        try (BufferedReader br = new BufferedReader(new FileReader(run.getLogFile()));
             MessageProducer producer = new MessageProducer()) {

            String sCurrentLine;

            int lineNumber = 1;

            while ((sCurrentLine = br.readLine()) != null) {
                JSONObject line = new JSONObject();
                line.put("environment", this.processEnvironment(run, new LogTaskListener(log, Level.INFO)));
                line.put("lineNumber", lineNumber++);
                line.put("message", sCurrentLine);
                producer.sendMessage(KafkaMetricsPluginConfig.get().getLogTopic(), line.toString());
            }
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }
}