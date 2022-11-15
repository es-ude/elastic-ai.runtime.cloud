# How to contribute

## Naming Scheme

|     Scope | Scheme               | Notes |
|----------:|:---------------------|:------|
|     Files | PascalCase           |       |
|     Class | PascalCase           |       |
| Functions | camelCase            |       |
| Variables | camelCase            |       |
| Constants | SCREAMING_SNAKE_CASE |       |


## Publish Modifications

Don't push directly to the `main` branch. Push your modification to a new branch and open a pull request to `main`, so
that the maintainer of this repository can merge your modifications.

## Class Structure

```mermaid
%%{init: {"theme": "forest"} }%%
classDiagram
  class Twin {
    #String identifier
    #CommunicationEndpoint endpoint

    +Twin(String identifier)

    +bindToCommunicationEndpoint(CommunicationEndpoint endpoint) void
    #subscribe(String topic, Subscriber subscriber) void
    #unsubscribe(String topic, Subscriber subscriber) void
    #publish(Posting posting) void
    #executeOnBind() void
    +ID() String
    +getEndpoint() CommunicationEndpoint
  }
  Twin ..> Subscriber
  Twin ..> Posting
  Twin ..> "1" CommunicationEndpoint

  class JavaTwin {
    +JavaTwin(String identifier)

    # executeOnBind() void
    +publishData(String dataId, String value) void
    +publishStatus(boolean online) void
    +subscribeForDataStartRequest(String dataId,Subscriber subscriber) void
    +unsubscribeFromDataStartRequest(String dataId, Subscriber subscriber) void
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

    # executeOnBind() void
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
    -String clientId
    -String mqttDomian
    -String brokerIp
    -int brokerPort
    -Mqtt5AsyncClient client

    +HivemqBroker(String mqttDomain, String brokerIp, String brokerPort, String clientId)

    -connectToClient(String identifierString, String ip, int port) void
    +connectWithoutKeepalive() void
    +connectWithKeepaliveAndLwtMessage() void
    +closeConnection() void
    +publish(Posting posting) void
    -onPublishComplete(Mqtt5Publishresult pubAck, Throwable throwable) void
    +subscribe(String topic, Subscriber subscriber) void
    +subscribeRaw(String topic, Subscriber subscriber) void
    -onSubscribeComplete(Throwable subFailed, String topic) void
    +unsubscribe(String topic, Subscriber subscriber) void
    +unsubscribeRaw(String topic, Subscriber subscriber) void
    -onUnsubscribeComplete(Throwable unsubFailed, String topic) void
    +getConfiguration() Dictionary<String, String>
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

    +isActive() boolean
    +setActive() void
    +setInactive() void
    +getName() String
    +setName(String name) void
    +getId() String
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
  TwinList *-- TwinData

  class MonitorTwin {
    -StatusMonitor monitor
    -TwinList twins

    +MonitorTwin(String id)
    
    + getTwinList

  }
  MonitorTwin --|> JavaTwin
  MonitorTwin o-- TwinList
  MonitorTwin o-- StatusMonitor
  MonitorTwin ..> CommunicationEndpoint
  
  class StatusMonitor {
    -JavaTwin owner
    -TwinStub stub
    -TwinList twins
  
    +StatusMonitor(JavaTwin owner, TwinList twins)
  
    -createTwinStubAndSubscribeForStatus() void  
    +deliver(Posting posting
  }
  StatusMonitor --|> Subscriber
  StatusMonitor o-- Posting
  StatusMonitor o-- JavaTwin
  StatusMonitor o-- TwinStub

  ENv5TwinStub --|> TwinStub
  IntegrationTestTwinForENv5 --|> JavaTwin
```
