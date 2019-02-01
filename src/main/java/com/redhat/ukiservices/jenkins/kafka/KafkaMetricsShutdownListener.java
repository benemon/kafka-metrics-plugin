package com.redhat.ukiservices.jenkins.kafka;

import com.redhat.ukiservices.jenkins.kafka.common.CommonConstants;
import com.redhat.ukiservices.jenkins.kafka.common.PayloadType;
import com.redhat.ukiservices.jenkins.kafka.job.base.AbstractKafkaMetricsPluginRunListener;
import hudson.Extension;
import hudson.model.listeners.ItemListener;
import jenkins.YesNoMaybe;
import net.sf.json.JSONObject;

import java.util.logging.Logger;

@Extension(dynamicLoadable = YesNoMaybe.YES)
public class KafkaMetricsShutdownListener extends ItemListener implements DefaultKafkaMetricsListener {

    private static final Logger log = Logger.getLogger(AbstractKafkaMetricsPluginRunListener.class.getName());

    @Override
    public void onBeforeShutdown() {
        JSONObject payload = new JSONObject();
        payload.put(CommonConstants.METADATA, this.createMetadata(PayloadType.DEREGISTER));
        JSONObject data = new JSONObject();
        payload.put(CommonConstants.DATA, data);
        this.sendMessage(payload);
        super.onBeforeShutdown();
    }
}
