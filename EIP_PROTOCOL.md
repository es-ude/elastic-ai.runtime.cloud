# EIP Protocol (v0.2)

The eip protocol is a specification of a publish/subscribe based star topology with a broker at the center.
Typically, this will be implemented via MQTT, but this is **not** fixed.
The basic functionality provided are _subscribing_, _unsubscribing_ and _publishing_ to and from topics and topic filters, together with a keepalive mechanism with last will message.

## Message

A message consists of a topic combined with a payload/data that should be published to the given topic.

### Topic

A topic is constructed as followed:

```
eip://<broker_domain>/<object_id>/<message_type>/<type_dependent>
```

where the `<object_id>` is the individual identifier of the target device and the `<message_type>/<type_dependant>` is defined as in the part [Message Types](#message-types).

### Message Types

|   Type | Description                                                       |
| -----: | :---------------------------------------------------------------- |
| STATUS | a message containing the online(1)/offline(0) status of an object |
|  START | a message requesting an object to start sending a stream of data  |
|   STOP | a message requesting an object to stop sending a stream of data   |
|   DATA | a message containing data                                         |
|     DO | a message containing a command                                    |
|   DONE | a message containing the response to a DO message                 |

#### STATUS

- Topic: `eip://<broker_domain>/<object_id>/STATUS`
- Data: `<object_id>` AND 0 (offline) OR 1 (online)
- Information:
  - The retain-flag of this message should be set to assure status discovery for newly entered participants!
  - The offline message should be automatically send by the broker after connection loss (LWT message).
  - The online message should only be sent by a new hardware device or Application Twin.

Example message:

```text
("eip://uni-due.de/es/twin1/STATUS","twin1;1")
("eip://uni-due.de/es/twin1/STATUS","twin1;0")
```

Communication Specification:

```mermaid
sequenceDiagram
  participant m as MonitorTwin
  participant t as Twin for Device
  participant b as Broker
  participant d as Device

  m ->> b: sub("eip://<broker_domain>/+/STATUS")
  Note over b,d: ESTABLISH CONNECTION
  d ->> b: pub("eip://<broker_domain>/env5_1/STATUS","env5_1#59;1")
  b ->> m: ("eip://<broker_domain>/env5_1/STATUS","env5_1#59;1")
  m -) t: start twin
  activate t
  Note over m,d: ...
  Note over d: LOST CONNECTION
  b ->> m: ("eip://<broker_domain>/env5_1/STATUS","env5_1#59;0")
  m -) t: stop twin
  deactivate t
```

#### START

- Topic: `eip://<broker_domain>/<object_id>/START/<data_id>`
- Data: topic specifying `eip://<broker_domain>/<object_id>` that is interested in data

Example message:

```text
("eip://uni-due.de/es/twin1/START/light","eip://uni-due.de/es/twin2")
```

#### STOP

- Topic: `eip://<broker_domain>/<object_id>/STOP/<data_id>`
- Data: topic specifying `eip://<broker_domain>/<object_id>` that was interested in data

Example message:

```text
("eip://uni-due.de/es/twin1/STOP/light","eip://uni-due.de/es/twin2")
```

#### DATA

- Topic: `eip://<broker_domain>/<object_id>/DATA/<data_id>`
- Data: value encoded as a string
- INFO:
  - Interested nodes can subscribe to this topic to receive new data

Example message:

```text
("eip://uni-due.de/es/twin1/DATA/light","30.7")
```

Communication specification for data that is published **continuously**:

```mermaid
sequenceDiagram
  participant t1 as Twin 1
  participant t2 as Twin 2
  participant b as Broker
  participant d as Device

  t2 ->> b: pub("eip://<broker_domain>/twin2/DATA/d1", "<val>")
  t1 ->> b: sub("eip://<broker_domain>/twin2/DATA/d1")
  t2 ->> b: pub("eip://<broker_domain>/twin2/DATA/d1", "<val>")
  b ->> t1: ("eip://<broker_domain>/twin2/DATA/d1", "<val>")
  t2 ->> b: pub("eip://<broker_domain>/twin2/DATA/d1", "<val>")
  b ->> t1: ("eip://<broker_domain>/twin2/DATA/d1", "<val>")
```

Communication for data that has to be **requested**:

```mermaid
sequenceDiagram
  participant t1 as Twin 1
  participant t2 as Twin 2
  participant b as Broker
  participant d as Device

  t1 ->> b: sub("eip://<broker_domain>/twin2/DATA/d1")
  t1 ->> b: pub("eip://<broker_domain>/twin2/START/d1","eip://<broker_domain>/twin1")
  b ->> t2: ("eip://<broker_domain>/twin2/START/d1","eip://<broker_domain>/twin1")
  t2 ->> b: sub("eip://<broker_domain>/twin1/STATUS")
  Note over t2,d: REQUEST DATA FROM DEVICE
  Note over t2: START SENDING DATA
  t2 ->> b: pub("eip://<broker_domain>/twin2/DATA/d1","<val>")
  b ->> t1: ("eip://<broker_domain>/twin2/DATA/d1","<val>")
  Note over t1,b: ...
  alt Twin 1 send STOP
  t1 ->> b: pub("eip://<broker_domain>/twin2/STOP/d1","eip://<broker_domain>/twin1")
  b ->> t2: ("eip://<broker_domain>/twin2/STOP/d1","eip://<broker_domain>/twin1")
  else Twin 1 lost connection
  Note over t1: LOST CONNECTION
  b ->> t2: ("eip://<broker_domain>/twin1/STATUS","0")
  end
  Note over t2: STOP SENDING DATA
  t2 ->> b: unsub("eip://<broker_domain>/twin1/STATUS")
```

#### DO

- Topic: `eip://<broker_domain>/<object_id>/DO/<command>`
- Data: command specific

Example message:

```text
("eip://uni-due.de/es/twin1/DO/SET/led/1","1")
```

Communication specification for a command **without** response:

```mermaid
sequenceDiagram
  participant t1 as Twin 1
  participant t2 as Twin 2
  participant b as Broker
  participant d as Device

  t2 ->> b: sub("eip://<broker_domain>/twin2/DO/<cmd1>")
  t1 ->> b: pub("eip://<broker_domain>/twin2/DO/<cmd1>","<val>")
  b ->> t2: ("eip://<broker_domain>/twin2/DO/<cmd1>","<val>")
  Note over t2,d: EXECUTE COMMAND
```

#### DONE

- Topic: `eip://<broker_domain>/<object_id>/DONE/<command>`
- Data: command specific

Example message:

```text
("eip://uni-due.de/es/twin1/DONE/SET/led/1","1")
```

Communication specification for a command **with** response:

```mermaid
sequenceDiagram
  participant t1 as Twin 1
  participant t2 as Twin 2
  participant b as Broker
  participant d as Device

  t2 ->> b: sub("eip://<broker_domain>/twin2/DO/<cmd1>")
  t1 ->> b: sub("eip://<broker_domain>/twin2/DONE/<cmd1>")
  t1 ->> b: pub("eip://<broker_domain>/twin2/DO/<cmd1>","<val>")
  b ->> t2: ("eip://<broker_domain>/twin2/DO/<cmd1>","<val>")
  Note over t2,d: EXECUTE COMMAND
  alt successful
    t2 ->> b: pub("eip://<broker_domain>/twin2/DONE/<cmd1>","<success>")
    b ->> t1: ("eip://<broker_domain>/twin2/DONE/<cmd1>","<success>")
  else unsuccessful
    t2 ->> b: pub("eip://<broker_domain>/twin2/DONE/<cmd1>","<failed>")
    b ->> t1: ("eip://<broker_domain>/twin2/DONE/<cmd1>","<failed>")
  end
```
