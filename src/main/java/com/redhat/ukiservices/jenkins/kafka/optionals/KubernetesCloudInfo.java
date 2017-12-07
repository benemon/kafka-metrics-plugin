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

import hudson.Extension;
import hudson.slaves.Cloud;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.csanchez.jenkins.plugins.kubernetes.ContainerTemplate;
import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud;
import org.csanchez.jenkins.plugins.kubernetes.PodImagePullSecret;
import org.csanchez.jenkins.plugins.kubernetes.PodTemplate;
import org.csanchez.jenkins.plugins.kubernetes.model.TemplateEnvVar;

import java.util.List;

@Extension(optional = true)
public class KubernetesCloudInfo {

    public KubernetesCloudInfo() {
    }

    /**
     * Returns a JSONArray containing the JSON representation of the Cloud config
     *
     * @return JSONArray
     */
    public JSONArray getKubeClouds() {
        JSONArray kubeClouds = new JSONArray();

        for (Cloud cloud : Jenkins.getInstance().clouds) {
            if (cloud instanceof KubernetesCloud) {
                JSONObject cloudDefinition = new JSONObject();

                cloudDefinition.put("name", cloud.getDisplayName());
                cloudDefinition.put("kubernetesURL", ((KubernetesCloud) cloud).getServerUrl());
                cloudDefinition.put("jenkinsURL", ((KubernetesCloud) cloud).getJenkinsUrl());
                cloudDefinition.put("templates", getCloudConfig((KubernetesCloud) cloud));

                kubeClouds.add(cloudDefinition);
            }
        }
        return kubeClouds;
    }

    /**
     * Get the CLOUDS present in the Kubernetes Cloud plugin configuration
     *
     * @param cloud
     * @return JSONObject
     */
    private JSONArray getCloudConfig(KubernetesCloud cloud) {
        JSONArray kubeClouds = new JSONArray();

        List<PodTemplate> templates = cloud.getTemplates();

        for (PodTemplate template : templates) {
            JSONObject cloudConfig = new JSONObject();
            cloudConfig.put("displayName", template.getDisplayName());
            cloudConfig.put("name", template.getName());
            cloudConfig.put("label", template.getLabel());
            cloudConfig.put("namespace", template.getNamespace());
            cloudConfig.put("instanceCap", template.getInstanceCap());
            cloudConfig.put("containers", this.getContainerConfig(template.getContainers()));
            cloudConfig.put("env", this.getEnvVars(template.getEnvVars()));
            cloudConfig.put("pullSecrets", this.getImagePullSecrets(template.getImagePullSecrets()));
            kubeClouds.add(cloudConfig);
        }

        return kubeClouds;
    }

    /**
     * Get the CONTAINERS present in the template and create a JSONArray
     *
     * @param cList
     * @return JSONArray
     */
    private JSONArray getContainerConfig(List<ContainerTemplate> cList) {
        JSONArray containers = new JSONArray();

        for (ContainerTemplate c : cList) {
            JSONObject container = new JSONObject();
            JSONObject resources = new JSONObject();
            JSONObject limit = new JSONObject();
            JSONObject request = new JSONObject();

            container.put("name", c.getName());
            container.put("displayName", c.getDisplayName());
            container.put("image", c.getImage());
            container.put("args", c.getArgs());
            container.put("alwaysPullImage", c.isAlwaysPullImage());
            container.put("command", c.getCommand());

            limit.put("cpu", c.getResourceLimitCpu());
            limit.put("memory", c.getResourceLimitMemory());
            request.put("cpu", c.getResourceRequestCpu());
            request.put("memory", c.getResourceRequestMemory());
            container.put("request", request);
            container.put("limit", limit);
            container.put("resources", resources);
            container.put("env", getEnvVars(c.getEnvVars()));
            containers.add(container);
        }

        return containers;
    }

    /**
     * Processes the ENV VARS present in the template and create a JSONArray of the content
     *
     * @param varList
     * @return JSONArray
     */
    private JSONArray getEnvVars(List<TemplateEnvVar> varList) {
        JSONArray envvars = new JSONArray();

        for (TemplateEnvVar var : varList) {
            JSONObject obj = new JSONObject();
            obj.put("name", var.buildEnvVar().getName());
            envvars.add(obj);
        }

        return envvars;
    }


    /**
     * Process the Image Pull Secrets present in the template and create a JSONArray of the content
     *
     * @param pList
     * @return JSONArray
     */
    private JSONArray getImagePullSecrets(List<PodImagePullSecret> pList) {
        JSONArray pullSecrets = new JSONArray();

        for (PodImagePullSecret ps : pList) {
            JSONObject obj = new JSONObject();
            obj.put("name", ps.getName());
            pullSecrets.add(obj);
        }

        return pullSecrets;
    }
}

