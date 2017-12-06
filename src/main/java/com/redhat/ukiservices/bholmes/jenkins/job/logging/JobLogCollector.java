package com.redhat.ukiservices.bholmes.jenkins.job.logging;

import com.redhat.ukiservices.bholmes.jenkins.configuration.KafkaMetricsPluginConfig;
import com.redhat.ukiservices.bholmes.jenkins.job.base.AbstractKafkaMetricsPluginRunListener;
import com.redhat.ukiservices.bholmes.jenkins.producer.MessageProducer;
import hudson.Extension;
import hudson.model.Run;
import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
                line.put("environment", this.processEnvironment(run));
                line.put("lineNumber", lineNumber++);
                line.put("message", sCurrentLine);
                producer.sendMessage(KafkaMetricsPluginConfig.get().getLogTopic(), line.toString());
            }
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }
}
