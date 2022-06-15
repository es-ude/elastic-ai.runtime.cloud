[![Unit Tests](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/unitTests.yml/badge.svg)](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/unitTests.yml)
[![Integration Tests](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/integrationTests.yml/badge.svg)](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/integrationTests.yml)

# elastic-AI.runtime

The elastic-AI.runtime provides a backend for operating digital twins.
It uses MQTT as a messaging protocol and is primarily focused on the use with the Elastic Node v5.
This repository uses the gradle multi-project feature and currently contains the following projects:

- runtime: elastic-AI.runtime

## Prerequisites

### Docker

The project needs the [Docker](https://www.docker.com/)-CLI to run the integration tests.
The integration tests need a MQTT Broker to run successful.

### MQTT Broker

The runtime uses MQTT as the main communication protocol, therefore an MQTT Broker is needed to run the code locally.
You can either install a broker on your machine or run it via docker.
The default for the project is [Mosquitto](https://mosquitto.org/) by Eclipse.
The elastic-AI.runtime communicates with the broker on port 1883.
If you want the broker to communicate with the elasticNode over the network, you may need to open the port 1883 on
your local machine's firewall.

#### Run Mosquitto (via Docker)

Run mosquitto via docker and use a `mosquitto.conf` file:

```bash
docker run -p 1883:1883 -p 9001:9001 -v $PWD/mosquitto.conf:/mosquitto/config/mosquitto.conf eclipse-mosquitto
```

The command will

1. Pull the latest `eclipse-mosquitto` image from [dockerhub](https://hub.docker.com/)
2. Start the container with the passed mosquitto.conf
3. Expose access to the container via Port 1883 (MQTT) and 9001

The `mosquitto.conf` file should at least embed the following settings:

```text
listener 1883 0.0.0.0
allow_anonymous true
```

## Experimental

This is meant as an RFC from my side for the team.
It proposes a new / different communication protocol and API for digital twins.

## Tests

Unit tests and integration tests can be executed independently. Both use _jacoco_ to finalize the tests with a report
that shows the test results and the code coverage.
The reports can be found in the location `build/reports/` relative to the corresponding build file.

### Test execution

|                          **Command** | **Task**                                                                                         |
|-------------------------------------:|:-------------------------------------------------------------------------------------------------|
|                        `gradle test` | Run **all** unit tests                                                                           |
|            `gradle: subproject:test` | Run unit test from **specific** subproject <br/> (i.g. `gradle :runtime:test`)                   |
|             `gradle integrationTest` | Run **all** Integration test                                                                     |
| `gradle :subproject:integrationTest` | Run integration test from **specific** subproject <br/> (i.g. `gradle: runtime:integrationTest`) |

## Monitor

To start the monitor run

```bash
gradle :monitor:run
```

The monitor can then be accessed locally at [localhost.com:8081](localhost.com:8081).
