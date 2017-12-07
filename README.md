# Kafka Publisher Plugin for Jenkins

### What is it?

A set of tasks and steps for Jenkins which publish:

- Metadata about the Jenkins environment to a given Kafka topic
- Metadata about a Jenkins Job to a given Kafka topic
- Log output from a Jenkins Job to a given Kafka topic
- A kafkaPublisher pipeline step to allow a user to publish bespoke information from a Pipeline to a given Kafka topic

### Usage

* Requires Minimum Jenkins version of 2.73.x *

`mvn hpi:hpi`

Install the resulting package as a plugin in your Jenkins instance.

Configure using Global System Configuration. Options for the following are provided:

- Broker Hosts - Comma separated list of the form host:port
- Metrics Topic - the topic to publish metrics and metadata from Jenkins and its jobs.
- Logs topic - the topic to publish log file entries from the Jenkins jobs.

* NOTE: There is nothing stopping you from having the same topic defined for both logs and metrics *

If you do nothing else, you should start to see traffic appear on your topic from your Jenkins instance and its job.

If you want to publish information as part of a Pipeline step, you can use the following syntax:

`kafkaPublisher ( topic: <topic>, payload: <payload> )`

The `topic` parameter is optional, and if not provided, will publish payloads to the default logging topic.

### Support

This plugin is provided on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.






