package com.redhat.ukiservices.jenkins.kafka.common;

public interface CommonConstants {

    // Environment Information
    String PROJECT_NAME_ENV = "PROJECT_NAME";
    String KUBERNETES_NAMESPACE_ENV = "KUBERNETES_NAMESPACE";
    String JENKINS_URL_ENV = "JENKINS_URL";
    String JOB_NAME_ENV = "JOB_NAME";
    String HOSTNAME_ENV = "HOSTNAME";

    // Payload (Meta)Data
    String EVENT_TYPE_KEY = "type";
    String METADATA = "metadata";
    String ENVIRONMENT = "environment";
    String PLUGINS = "plugins";
    String JOB_NAME = "name";
    String ESTIMATED_DURATION = "estimatedDuration";
    String ACTUAL_DURATION = "actualDuration";
    String RESULT = "result";
    String LOG_LINE_NUMBER = "lineNumber";
    String LOG_MESSAGE = "message";
    String JOB = "job";
    String DATA = "data";
}

