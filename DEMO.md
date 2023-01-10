# DEMO

[Simplified Overview](#Simplified-Overview)

[Detailed Overview](#Detailed-Overview)

[Simplified Device Loses Connection](#Simplified-Device-Loses-Connection)

[Simplified Twin Loses Connection](#Simplified-Twin-Loses-Connection)

## Simplified Overview

PowerConsumption twin gets online and waits for device, when device comes online data is requested.
After some time data is requested no longer.
Twin Stub included in PowerConsumptionTwin.
Requested Values combined into VALUE.

```mermaid
sequenceDiagram
      participant pw as Power Consumption Twin
      participant b as Broker
      participant d as ENv5

    pw ->> b: pub("eip://DOMAIN/powerConsumptionTwin/STATUS", ONLINE)
    pw ->> b: sub("eip://DOMAIN/ENv5/STATUS")
    
    Note over pw: WAITS FOR DEVICE
    
    d ->> b: sub("eip://DOMAIN/ENv5/START/Value")
        
    d ->> b: pub("eip://DOMAIN/ENv5/STATUS", ONLINE)
    b ->> pw: pub("eip://DOMAIN/ENv5/STATUS", ONLINE)
 
    pw ->> b: pub("eip://DOMAIN/ENv5/START/Value")
    d ->> b: sub("eip://DOMAIN/powerConsumptionTwin/STATUS")
    
    pw ->> b: pub("eip://DOMAIN/ENv5/START/Value")
    b ->> d: pub("eip://DOMAIN/ENv5/START/Value")
    
    Note over d: GETS MEASUREMENT
        
    d ->> b: pub("eip://DOMAIN/ENv5/DATA/Value")
    b ->> pw: pub("eip://DOMAIN/ENv5/DATA/Value")
    
    Note over pw,d: ...
    
    pw ->> b: pub("eip://DOMAIN/ENv5/STOP/Value")
    d ->> b: unsub("eip://DOMAIN/powerConsumptionTwin/STATUS")
```

## Detailed Overview

PowerConsumption twin gets online and waits for device, when device comes online data is requested. After some time data is requested no longer.

```mermaid
sequenceDiagram
      participant pw as Power Consumption Twin
      participant ds as ENv5 Stub
      participant b as Broker
      participant d as ENv5

    pw ->> b: pub("eip://DOMAIN/powerConsumptionTwin/STATUS", ONLINE)
    pw ->> ds: subscribeForStatus
    ds ->> b: sub("eip://DOMAIN/ENv5/STATUS")
    
    Note over pw, ds: WAITS FOR DEVICE
    
    d ->> b: sub("eip://DOMAIN/ENv5/START/wifiValue")
    d ->> b: sub("eip://DOMAIN/ENv5/START/sRamValue")
    
    d ->> b: pub("eip://DOMAIN/ENv5/STATUS", ONLINE)
    b ->> ds: pub("eip://DOMAIN/ENv5/STATUS", ONLINE)
    ds ->> pw: deviceStatus
 
    pw ->> ds: publishDataStartRequest
    ds ->> b: pub("eip://DOMAIN/ENv5/START/wifiValue")
    b ->> d: pub("eip://DOMAIN/ENv5/START/wifiValue")
    d ->> b: sub("eip://DOMAIN/powerConsumptionTwin/STATUS")
    
    pw ->> ds: publishDataStartRequest
    ds ->> b: pub("eip://DOMAIN/ENv5/START/sRamValue")
    b ->> d: pub("eip://DOMAIN/ENv5/START/sRamValue")
    
    Note over d: GETS MEASUREMENT
    d ->> b: pub("eip://DOMAIN/ENv5/DATA/wifiValue")
    b ->> ds: pub("eip://DOMAIN/ENv5/DATA/wifiValue")
    ds ->> pw: setWifiValue
    
    Note over d: GETS MEASUREMENT
    d ->> b: pub("eip://DOMAIN/ENv5/DATA/sRamValue")
    b ->> ds: pub("eip://DOMAIN/ENv5/DATA/sRamValue")
    ds ->> pw: setSRamValue
    
    Note over pw,d: ...
    
    pw ->> ds: publishDataStopRequest
    ds ->> b: pub("eip://DOMAIN/ENv5/STOP/wifiValue")
    b ->> d: pub("eip://DOMAIN/ENv5/STOP/wifiValue")
    
    pw ->> ds: publishDataStopRequest
    ds ->> b: pub("eip://DOMAIN/ENv5/STOP/sRamValue")
    b ->> d: pub("eip://DOMAIN/ENv5/STOP/sRamValue")
    d ->> b: unsub("eip://DOMAIN/powerConsumptionTwin/STATUS")
```

## Simplified Device Loses Connection

```mermaid
sequenceDiagram
      participant pw as Power Consumption Twin
      participant b as Broker
      participant d as ENv5

    pw ->> b: pub("eip://DOMAIN/powerConsumptionTwin/STATUS", ONLINE)
    pw ->> b: sub("eip://DOMAIN/ENv5/STATUS")
    
    Note over pw: WAITS FOR DEVICE
    
    d ->> b: sub("eip://DOMAIN/ENv5/START/Value")
        
    d ->> b: pub("eip://DOMAIN/ENv5/STATUS", ONLINE)
    b ->> pw: pub("eip://DOMAIN/ENv5/STATUS", ONLINE)
 
    pw ->> b: pub("eip://DOMAIN/ENv5/START/Value")
    b ->> d: pub("eip://DOMAIN/ENv5/START/Value")
    d ->> b: sub("eip://DOMAIN/powerConsumptionTwin/STATUS")
    
    Note over d: GETS MEASUREMENT
        
    d ->> b: pub("eip://DOMAIN/ENv5/DATA/Value")
    b ->> pw: pub("eip://DOMAIN/ENv5/DATA/Value")
    
    Note over d: LOSES CONNECTION
        
    b ->> pw:  pub("eip://DOMAIN/ENv5/STATUS", OFFLINE)
      
    Note over d: RECONNECTS

    d ->> b: sub("eip://DOMAIN/ENv5/START/Value")
        
    d ->> b: pub("eip://DOMAIN/ENv5/STATUS", ONLINE)
    b ->> pw: pub("eip://DOMAIN/ENv5/STATUS", ONLINE)
 
    pw ->> b: pub("eip://DOMAIN/ENv5/START/Value")
    b ->> d: pub("eip://DOMAIN/ENv5/START/Value")
    d ->> b: sub("eip://DOMAIN/powerConsumptionTwin/STATUS")
    
    Note over d: GETS MEASUREMENT
        
    d ->> b: pub("eip://DOMAIN/ENv5/DATA/Value")
    b ->> pw: pub("eip://DOMAIN/ENv5/DATA/Value")
```

## Simplified Twin Loses Connection

```mermaid
sequenceDiagram
      participant pw as Power Consumption Twin
      participant b as Broker
      participant d as ENv5

    pw ->> b: pub("eip://DOMAIN/powerConsumptionTwin/STATUS", ONLINE)
    pw ->> b: sub("eip://DOMAIN/ENv5/STATUS")
    
    Note over pw: WAITS FOR DEVICE
    
    d ->> b: sub("eip://DOMAIN/ENv5/START/Value")
        
    d ->> b: pub("eip://DOMAIN/ENv5/STATUS", ONLINE)
    b ->> pw: pub("eip://DOMAIN/ENv5/STATUS", ONLINE)
 
    pw ->> b: pub("eip://DOMAIN/ENv5/START/Value")
    b ->> d: pub("eip://DOMAIN/ENv5/START/Value")
    d ->> b: sub("eip://DOMAIN/powerConsumptionTwin/STATUS")
    
    Note over d: GETS MEASUREMENT
        
    d ->> b: pub("eip://DOMAIN/ENv5/DATA/Value")
    b ->> pw: pub("eip://DOMAIN/ENv5/DATA/Value")
    
    Note over pw: LOSES CONNECTION
        
    b ->> d:  pub("eip://DOMAIN/powerConsumptionTwin/STATUS", OFFLINE)
        
    d ->> b: unsub("eip://DOMAIN/powerConsumptionTwin/STATUS")
            
    Note over d: STOPS PUBLISHING DATA
      
    Note over pw: RECONNECTS


    pw ->> b: pub("eip://DOMAIN/powerConsumptionTwin/STATUS", ONLINE)
    pw ->> b: sub("eip://DOMAIN/ENv5/STATUS")
    
    b ->> pw: pub("eip://DOMAIN/ENv5/STATUS", ONLINE)
 
    pw ->> b: pub("eip://DOMAIN/ENv5/START/Value")
    b ->> d: pub("eip://DOMAIN/ENv5/START/Value")
    d ->> b: sub("eip://DOMAIN/powerConsumptionTwin/STATUS")
    
    Note over d: GETS MEASUREMENT
        
    d ->> b: pub("eip://DOMAIN/ENv5/DATA/Value")
    b ->> pw: pub("eip://DOMAIN/ENv5/DATA/Value")
```
