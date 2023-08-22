# DEMO

For running the DEMO, four components are needed. An MQTT Broker, the Monitor, the EnV5 Twin, and a EnV5 Device.

The three software components can be started together by using the docker compose file.
For this the Twin and Monitor docker images need to be built beforehand (See [README](../README.md#docker-container)).

```bash
docker compose up
```

## Communication

The diagrams show simplified, how the parties communicate with each other in the DEMO.

### Status

```mermaid
sequenceDiagram
    participant communicationEndpoint as enV5Twin
    participant device as enV5
    participant monitorCommunicationEndpoint as Monitor
    participant broker as Broker

    communicationEndpoint ->> broker: STATUS
    device ->> broker: STATUS
    monitorCommunicationEndpoint ->> broker: STATUS
    broker ->> monitorCommunicationEndpoint: EnV5Twin STATUS

    broker ->> communicationEndpoint: Env5 STATUS
    communicationEndpoint ->> broker: UPDATED STATUS

    broker ->> monitorCommunicationEndpoint: EnV5Twin STATUS
```

### DATA

Broker removed for simplicity

```mermaid
sequenceDiagram
    participant monitorCommunicationEndpoint as Monitor
    participant communicationEndpoint as enV5Twin
    participant device as enV5

    monitorCommunicationEndpoint ->> communicationEndpoint: START
    communicationEndpoint ->> device: START

    device ->> communicationEndpoint: DATA
    communicationEndpoint ->> monitorCommunicationEndpoint: DATA

    monitorCommunicationEndpoint ->> communicationEndpoint: STOP
    communicationEndpoint ->> device: STOP
```

### FLASH

Broker removed for simplicity

```mermaid
sequenceDiagram
    participant monitorCommunicationEndpoint as Monitor
    participant communicationEndpoint as enV5Twin
    participant device as enV5

    monitorCommunicationEndpoint ->> communicationEndpoint: FLASH
    communicationEndpoint ->> device: FLASH

    device ->> monitorCommunicationEndpoint: REQUEST
    monitorCommunicationEndpoint ->> device: BITFILE

    device ->> communicationEndpoint: DONE
    communicationEndpoint ->> monitorCommunicationEndpoint: DONE
```
