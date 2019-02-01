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

package com.redhat.ukiservices.jenkins.kafka;

import com.redhat.ukiservices.jenkins.kafka.common.CommonConstants;
import com.redhat.ukiservices.jenkins.kafka.common.PayloadType;
import hudson.PluginWrapper;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Optional;

public class KafkaMetricsStartupListener implements DefaultKafkaMetricsListener {

    @Initializer(after = InitMilestone.JOB_LOADED)
    public void onStartup() {
        JSONObject payload = new JSONObject();
        payload.put(CommonConstants.METADATA, this.createMetadata(PayloadType.REGISTER));

        JSONObject data = new JSONObject();
        data.put(CommonConstants.PLUGINS, this.processPlugins());
        payload.put(CommonConstants.DATA, data);

        this.sendMessage(payload);
    }

    private JSONArray processPlugins() {
        JSONArray plugins = new JSONArray();

        Optional<Jenkins> jenkins = Optional.ofNullable(Jenkins.getInstanceOrNull());

        if (jenkins.isPresent()) {
            List<PluginWrapper> pluginList = jenkins.get().getInstance().pluginManager.getPlugins();

            for (PluginWrapper plugin : pluginList) {
                JSONObject pluginJson = new JSONObject();
                pluginJson.put("longName", plugin.getLongName());
                pluginJson.put("displayName", plugin.getDisplayName());
                pluginJson.put("shortName", plugin.getShortName());
                pluginJson.put("version", plugin.getVersion());
                plugins.add(pluginJson);
            }
        }
        return plugins;
    }

}
