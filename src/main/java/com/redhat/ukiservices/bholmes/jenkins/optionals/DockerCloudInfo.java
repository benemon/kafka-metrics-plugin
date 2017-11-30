package com.redhat.ukiservices.bholmes.jenkins.optionals;

import com.nirima.jenkins.plugins.docker.DockerCloud;
import com.nirima.jenkins.plugins.docker.DockerTemplate;
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
        JSONArray dockerCloud = new JSONArray();

        for (Cloud cloud : Jenkins.getInstance().clouds) {
            if (cloud instanceof DockerCloud) {
                dockerCloud.add(getCloudConfig((DockerCloud) cloud));
            }
        }
        return dockerCloud;
    }

    private JSONObject getCloudConfig(DockerCloud cloud) {
        JSONObject cloudConfig = new JSONObject();

        List<DockerTemplate> templates = cloud.getTemplates();

        for (DockerTemplate template : templates) {
            cloudConfig.put("displayName", template.getDisplayName());
            cloudConfig.put("label", template.getLabelString();
            cloudConfig.put("instanceCap", template.getInstanceCap());
            cloudConfig.put("containers", this.getContainerConfig(template.getContainers()));
            cloudConfig.put("env", this.getEnvVars(template.getEnvVars()));
            cloudConfig.put("pullSecrets", this.getImagePullSecrets(template.getImagePullSecrets()));
        }

        return cloudConfig;
    }
}
