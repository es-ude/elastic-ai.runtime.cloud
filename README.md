[![Tests](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/run_checks.yml/badge.svg)](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/run_checks.yml)
[![Create Release](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/push_to_main.yml/badge.svg)](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/push_to_main.yml)
[![Build And Push Container](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/build_container.yml/badge.svg)](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/build_container.yml)

# elastic-AI.runtime

The elastic-AI.runtime provides a cloud backend for operating digital twins.
It uses MQTT as a messaging protocol and is primarily focused to be used in combination with the Elastic Node v5.
This repository uses the gradle multi-project feature and currently contains the following projects:

-   elastic-ai.runtime:runtime
-   elastic-ai.runtime:monitor

## Setup

> You can check if your local machine satisfies the required dependencies by executing `test_setup.sh` script in the
> project [root directory](.).

### System Environment

The elastic-ai.runtime:monitor requires the Host-IP to be addressed.
The application will retrieve this information from a system environment variable called `HOST_IP`.
It is recommended that you run

```bash
# MacOS
export HOST_IP=127.0.0.1

# Linux
export HOST_IP=$(hostname -I)
```

before starting the monitor (with docker or locally).

### Java

Requires JDK Version **17** or higher.

### Docker

The project depends on the [Docker](https://www.docker.com/)-CLI to run the integration tests,
because a local MQTT Broker is needed to run as described in [MQTT Broker](#mqtt-broker).

### MQTT Broker

The runtime uses MQTT as the main communication protocol, therefore, an MQTT Broker is needed to run the code locally.
You can either install a broker on your machine or run it via docker.
The default for the project is [Mosquitto](https://mosquitto.org/) by Eclipse.
The elastic-AI.runtime communicates with the broker on port 1883.
If you want the broker to communicate with the elasticNode over the network, you may need to open port 1883 on your
local machine's firewall.

#### Run Mosquitto (native)

Check your local package manager.

#### Run Mosquitto (via Docker)

Run mosquitto via docker and the provided [mosquitto.conf](./mosquitto.conf) file:

```bash
docker run -p 1883:1883 -v $PWD/mosquitto.conf:/mosquitto/config/mosquitto.conf eclipse-mosquitto:1.6.14
```

The command will

1. Pull the 1.6.14 `eclipse-mosquitto` image from [dockerhub](https://hub.docker.com/)
2. Start the container with the passed mosquitto.conf
3. Expose access to the container via Port 1883 (MQTT)

The `mosquitto.conf` file should at least embed the following settings:

```text
listener 1883 0.0.0.0
allow_anonymous true
```

## Tests

Unit tests and integration tests can be executed independently. Both use _jacoco_ to finalize the tests with a report
that shows the test results and the code coverage.
The reports can be found in the location `build/reports/` relative to the corresponding build file.

### Test execution

| **Command**                             | **Task**                                                                                           |
| --------------------------------------- | -------------------------------------------------------------------------------------------------- |
| `./gradlew test`                        | Run ** all** unit tests                                                                            |
| `./gradlew :subproject:test`            | Run unit tests from ** specific** subproject <br/> (i.g. `gradle :runtime:test`)                   |
| `./gradlew integrationTest`             | Run ** all** Integration test                                                                      |
| `./gradlew :subproject:integrationTest` | Run integration tests from ** specific** subproject <br/> (i.g. `gradle: runtime:integrationTest`) |

### Monitor

The monitor is used to provide an external interface for the user to interact with the elastic-ai.runtime ecosystem.
This interface is provided via a Java web application, which can be accessed with every common browser (e.g. Chrome,
Firefox, Safari, ...).
To start the monitor locally via Gradle run

```bash
./gradlew :monitor:run
```

The broker domain and port can be passed to the monitor as follows:

```bash
./gradlew :monitor:run --args="-b localhost -p 1883"
```

The monitor can then be accessed locally at [http://localhost:8081](localhost.com:8081).

### Runtime

The runtime is meant to provide the necessary functions to implement a backend for the elastic-ai ecosystem.
It provides the necessary functions to operate the ecosystem, like the implementation of the Twin concept or the
HiveMQBroker implementation together with the necessary functions to handle the MQTT Broker interactions.

To start the runtime locally via Gradle run

```bash
./gradlew :runtime:run
```

### Exit Codes

| Exit Code | Description            |
| --------: | :--------------------- |
|         0 | No error               |
|        10 | Argument Parser failed |

## Docker

### Build Container

A docker container for a subproject can be created with:

```bash
./gradlew  :<subproject>:jibDockerBuild
```

This container can then be used in a docker-compose file or started manually.

### Run Container

A Container should be run with:

```bash
docker run --rm --network=runtime-network -p 8081:8081 --name <subproject> <subproject:tag>
```

The flags serve for the following purposes:

-   `--rm`: removes the container after shutdown
-   `--network`: required to access the docker container running the broker
    -   this is mandatory, as both containers have to be on the same network, otherwise the name resolution does not work
-   `-p`: Port mapping for the webserver port, which allows the monitor webinterface to be accessible from other host
    machines
-   `--name`: specifies the name of the container

#### Monitor

The Monitor needs to be run with the additional flag `-e "[HOST-IP]"`, where HOST-IP is the IP from which the monitor
will be reachable in the network.
When run in docker compose, the value needs to be set as an environmental variable `export HOST-IP="[HOST-IP]"`.
