package com.redhat.ukiservices.bholmes.jenkins.optionals;

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
        JSONArray dockerCloud = new JSONArray();

        for (Cloud cloud : Jenkins.getInstance().clouds) {
            if (cloud instanceof DockerCloud) {
                dockerCloud.add(getCloudConfig((DockerCloud) cloud));
            }
        }
        return dockerCloud;
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
