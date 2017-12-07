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

package com.redhat.ukiservices.jenkins.kafka.optionals;

import com.nirima.jenkins.plugins.docker.DockerCloud;
import com.nirima.jenkins.plugins.docker.DockerTemplate;
import com.nirima.jenkins.plugins.docker.DockerTemplateBase;
import hudson.Extension;
import hudson.slaves.Cloud;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;

@Extension(optional = true)
public class DockerCloudInfo {

    public DockerCloudInfo() {
    }

    public JSONArray getDockerClouds() {
        JSONArray dockerClouds = new JSONArray();

        for (Cloud cloud : Jenkins.getInstance().clouds) {
            if (cloud instanceof DockerCloud) {
                JSONObject cloudDefinition = new JSONObject();

                cloudDefinition.put("name", cloud.name);
                cloudDefinition.put("templates", getCloudConfig((DockerCloud) cloud));

                dockerClouds.add(cloudDefinition);
            }
        }
        return dockerClouds;
    }

    /**
     * Takes a Docker Cloud and returns a JSONObject representation
     *
     * @param cloud
     * @return
     */
    private JSONArray getCloudConfig(DockerCloud cloud) {
        JSONArray dockerCloud = new JSONArray();

        List<DockerTemplate> templates = cloud.getTemplates();

        for (DockerTemplate template : templates) {
            JSONObject cloudConfig = new JSONObject();
            cloudConfig.put("label", template.getLabelString());
            cloudConfig.put("description", template.getShortDescription());
            cloudConfig.put("instanceCap", template.getInstanceCap());
            cloudConfig.put("template", this.getDockerTemplateBase(template.getDockerTemplateBase()));
            dockerCloud.add(cloudConfig);
        }

        return dockerCloud;
    }

    /**
     * Take the Base template and return a JSONObject representation
     *
     * @param base
     * @return JSONObject
     */
    private JSONObject getDockerTemplateBase(DockerTemplateBase base) {
        JSONObject docker = new JSONObject();
        docker.put("name", base.getDisplayName());
        docker.put("cpu", base.getCpuShares());
        docker.put("dns", base.getDnsString());
        docker.put("cmd", base.getDockerCommandArray());
        docker.put("env", base.getEnvironmentsString());
        docker.put("fullImageId", base.getFullImageId());
        docker.put("image", base.getImage());
        docker.put("memory", base.getMemoryLimit());
        docker.put("pullCredentialsId", base.getPullCredentialsId());
        docker.put("registry", base.getRegistry().getUrl());
        docker.put("volumes", base.getVolumesString());

        return docker;
    }

}
