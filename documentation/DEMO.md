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
    participant twin as enV5Twin
    participant device as enV5
    participant monitor as Monitor
    participant broker as Broker

    twin ->> broker: STATUS
    device ->> broker: STATUS
    monitor ->> broker: STATUS
    broker ->> monitor: EnV5Twin STATUS

    broker ->> twin: Env5 STATUS
    twin ->> broker: UPDATED STATUS

    broker ->> monitor: EnV5Twin STATUS
```

### DATA

Broker removed for simplicity

```mermaid
sequenceDiagram
    participant monitor as Monitor
    participant twin as enV5Twin
    participant device as enV5

    monitor ->> twin: START
    twin ->> device: START

    device ->> twin: DATA
    twin ->> monitor: DATA

    monitor ->> twin: STOP
    twin ->> device: STOP
```

### FLASH

Broker removed for simplicity

```mermaid
sequenceDiagram
    participant monitor as Monitor
    participant twin as enV5Twin
    participant device as enV5

    monitor ->> twin: FLASH
    twin ->> device: FLASH

    device ->> monitor: REQUEST
    monitor ->> device: BITFILE

    device ->> twin: DONE
    twin ->> monitor: DONE
```
