[![Unit Tests](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/unitTests.yml/badge.svg)](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/unitTests.yml)
[![Integration Tests](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/integrationTests.yml/badge.svg)](https://github.com/es-ude/elastic-ai.runtime/actions/workflows/integrationTests.yml)

# elastic-AI.runtime

The elastic-AI.runtime provides a backend for operating digital twins.
It uses MQTT as a messaging protocol and is primarily focused on the use with the Elastic Node v5.
This repository uses the gradle multi-project feature and currently contains the following projects:

- elastic-ai.runtime:runtime
- elastic-ai.runtime:monitor

## Prerequisites

### Java

Requires Java Version **17**

### Docker

The project needs the [Docker](https://www.docker.com/)-CLI to run the integration tests, because a running MQTT Broker is needed to run successful as described in [MQTT Broker](#mqtt-broker).

### MQTT Broker

The runtime uses MQTT as the main communication protocol, therefore an MQTT Broker is needed to run the code locally.
You can either install a broker on your machine or run it via docker.
The default for the project is [Mosquitto](https://mosquitto.org/) by Eclipse.
The elastic-AI.runtime communicates with the broker on port 1883.
If you want the broker to communicate with the elasticNode over the network, you may need to open port 1883 on your local machine's firewall.

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

| **Command**                          | **Task**                                                                                          |
|--------------------------------------|---------------------------------------------------------------------------------------------------|
| `gradle test`                        | Run ** all** unit tests                                                                           |
| `gradle: subproject:test`            | Run unit test from ** specific** subproject <br/> (i.g. `gradle :runtime:test`)                   |
| `gradle integrationTest`             | Run ** all** Integration test                                                                     |
| `gradle :subproject:integrationTest` | Run integration test from ** specific** subproject <br/> (i.g. `gradle: runtime:integrationTest`) |

## Project Structure

```mermaid 
%%{init: {"theme": "forest", "fonFamily" : "monospace", "flowchart" : { "curve" : "linear"}} }%%
classDiagram
  class Twin {
    #String identifier
    #CommunicationEndpoint endpoint
    
    +Twin(String identifier)
    
    +bind(CommunicationEndpoint endpoint) void
    #subscribe(String topic, Subscriber subscriber) void
    #unsubscribe(String topic, Subscriber subscriber) void
    #publish(Posting posting) void
    #executeOnBind( ) void
    +ID( ) String
    +getEndpoint( ) CommunicationEndpoint
  }
  Twin "*" --o "1" CommunicationEndpoint
  Twin ..> Subscriber
  Twin ..> Posting
  
  class JavaTwin {
    +JavaTwin(String identifier)
    
    +publishData(String dataId, String value) void
    +publishStatus(boolean online) void
    +subscribeForStatus(String deviceId, Subscriber subscriber) void
    +unsubscribeFromStatus(String deviceId, Subscriber subscriber) void
    +subscribeForDataStartRequest(String dataId, Subscriber subscriber) void
    +void unsubscribeFromDataStartRequest(String dataId, Subscriber subscriber) void
    +subscribeForDataStopRequest(String dataId, Subscriber subscriber) void
    +unsubscribeFromDataStopRequest(String dataId, Subscriber subscriber) void
    +subscribeForCommand(String commandId, Subscriber subscriber) void
    +unsubscribeFromCommand(String commandId, Subscriber subscriber) void
  }
  JavaTwin --|> Twin
  JavaTwin ..> Subscriber
  JavaTwin ..> Posting
  
  class TwinStub {
    +TwinStub(String identifier)
    
    +subscribeForData(String dataId, Subscriber subsciber) void
    +unsubscribeFromData(String dataId, Scubscriber subscriber) void
    +subscribeForStatus(Subscriber subscriber) void
    +unsubscribeFromStatus(Subscriber subscriber) void
    +publishDataStartRequest(String dataId, String : receiver) void
    +publishDataStopRequest(String dataId, String receiver ) void
    +publishCommand(String service, String command) void
  }
  TwinStub --|> Twin
  TwinStub ..> Subscriber
  TwinStub ..> Posting

  class CommunicationEndpoint {
    <<interface>>
    publish(Posting posting)
    subscribe(String topic, Subscriber subscriber) void
    subscribeRaw(String topic, Subscriber subscriber) void
    unsubscribe(String topic, Subscriber subscriber) void
    unsubscribeRaw(String topic, Subscriber subscriber) void
    ID() String
  }
  CommunicationEndpoint ..> Subscriber
  CommunicationEndpoint ..> Posting
  
  class HivemqBroker {
    -String identifier
    -Mqtt5AsyncClient client
    
    +HivemqBroker(String identifier)
    +HivemqBroker(String identifier, String ip, String port)
    
    -connectToClient(String identifierString, String ip, int port) void
    +closeConnection() void
    +publish(Posting posting) void
    -onPublishComplete(Mqtt5Publishresult pubAck, Throwable throwable) void
    +subscribe(String topic, Subscriber subscriber) void
    +subscribeRaw(String topic, Subscriber subscriber) void
    -onSubscribeComplete(Throwable subFailed, String topic) void
    +unsubscribe(String topic, Subscriber subscriber) void
    +unsubscribeRaw(String topic, Subscriber subscriber) void
    -onUnsubscribeComplete(Throwable unsubFailed, String topic) void
    +ID() String
  }
  HivemqBroker ..|> CommunicationEndpoint
  HivemqBroker ..> Subscriber
  HivemqBroker ..> Posting
  
  class PostingType {
    <<enumeration>>
    DATA
    START
    STOP
    SET
    LOST
    STATUS
    
    +topic(String topicID) String
  }
  
  class Posting {
    <<record>>
    +Posting(String topic, String data)
    
    +createCommand(String topic, String command) Posting
    +createStartSending(String dataId, String receiver) Posting
    +createStopSending(String dataId, String receiver) Posting
    +createData(String dataId, String value) Posting
    +createStatus(String deviceId, boolean online) Posting
    +cloneWithTopicAffix(String affix) Posting
    +isStartSending(String topic) : boolean
  }
  Posting ..> PostingType
  
  class Subscriber {
    <<interface>>
    +deliver (Posting posting) void
  }
  Subscriber ..> Posting
  
  class TwinData {
    -String name
    -String ID
    -boolean active
    
    +TwinData(String name, String ID)
    
    +setActive() void
    +setInactive() void
    +getName() String
    +setName(String name) void
    +ID() String
    +isActive() boolean
    +toString() String
  }
  
  class TwinList {
    -List<TwinData> twins
    
    +TwinList()
    
    +changeTwinName(String ID, String newName) void
    +getTwin(Strind ID) TwinData
    +addTwin(String ID) void
    +getActiveTwins() List<TwinData>
    +getTwins() List<TwinData>
  }
  TwinList *-- "*" TwinData
  
  class TwinStatusMonitor {
    -Subscriber statusSubscriber
    -TwinStub twin
    
    +TwinStatusMonitor(TwinList twinList) void
    
    +bind(CommunicationEndpoint endpoint) void
  }
  TwinStatusMonitor *-- "1" Subscriber
  TwinStatusMonitor *-- "1" TwinList
  TwinStatusMonitor --o "1" CommunicationEndpoint
  
  ENv5TwinStub --|> TwinStub
  IntegrationTestTwin --|> JavaTwin
```

### Monitor

The monitor is used to provide an external interface for user to interact with the elastic-ai ecosystem.
This interface is provided via a java web application, which can be accessed via every common browser (e.g. Chrome, Firefox, Safari, ...).
To start the monitor run

```bash
./gradlew :monitor:run
```

The monitor can then be accessed locally at [http://localhost.com:8081](localhost.com:8081).

#### Exit Codes

| Exit Code | Description            |
|----------:|:-----------------------|
|         0 | No error               |
|        10 | Argument Parser failed |

### Runtime

The runtime is meant to provide the necessary functions to implement a backend for the elastic-ai ecosystem.
It provides the necessary function to operate the ecosystem, like the implementation of the Twin concept or the HiveMQBroker implementation together with the necessary functions to handle the MQTT Broker interactions.
