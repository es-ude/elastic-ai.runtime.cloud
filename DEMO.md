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
      participant dt as enV5 Twin
      participant b as Broker
      participant d as enV5

    dt ->> b: pub("eip://DOMAIN/enV5Twin/STATUS", ONLINE)
    dt ->> b: sub("eip://DOMAIN/enV5/STATUS")
    
    Note over dt: WAITS FOR DEVICE
    
    d ->> b: sub("eip://DOMAIN/enV5/START/Value")
        
    d ->> b: pub("eip://DOMAIN/enV5/STATUS", ONLINE)
    b ->> dt: pub("eip://DOMAIN/enV5/STATUS", ONLINE)
 
    dt ->> b: pub("eip://DOMAIN/enV5/START/Value")
    d ->> b: sub("eip://DOMAIN/enV5Twin/STATUS")
    
    dt ->> b: pub("eip://DOMAIN/enV5/START/Value")
    b ->> d: pub("eip://DOMAIN/enV5/START/Value")
    
    Note over d: GETS MEASUREMENT
        
    d ->> b: pub("eip://DOMAIN/enV5/DATA/Value")
    b ->> dt: pub("eip://DOMAIN/enV5/DATA/Value")
    
    Note over dt,d: ...
    
    dt ->> b: pub("eip://DOMAIN/enV5/STOP/Value")
    d ->> b: unsub("eip://DOMAIN/enV5Twin/STATUS")
```

## Detailed Overview

PowerConsumption twin gets online and waits for device, when device comes online data is requested. After some time data is requested no longer.

```mermaid
sequenceDiagram
      participant dt as enV5 Twin
      participant ds as enV5 Stub
      participant b as Broker
      participant d as enV5

    dt ->> b: pub("eip://DOMAIN/enV5Twin/STATUS", ONLINE)
    dt ->> ds: subscribeForStatus
    ds ->> b: sub("eip://DOMAIN/enV5/STATUS")
    
    Note over dt, ds: WAITS FOR DEVICE
    
    d ->> b: sub("eip://DOMAIN/enV5/START/wifiValue")
    d ->> b: sub("eip://DOMAIN/enV5/START/sRamValue")
    
    d ->> b: pub("eip://DOMAIN/enV5/STATUS", ONLINE)
    b ->> ds: pub("eip://DOMAIN/enV5/STATUS", ONLINE)
    ds ->> dt: deviceStatus
 
    dt ->> ds: publishDataStartRequest
    ds ->> b: pub("eip://DOMAIN/enV5/START/wifiValue")
    b ->> d: pub("eip://DOMAIN/enV5/START/wifiValue")
    d ->> b: sub("eip://DOMAIN/enV5Twin/STATUS")
    
    dt ->> ds: publishDataStartRequest
    ds ->> b: pub("eip://DOMAIN/enV5/START/sRamValue")
    b ->> d: pub("eip://DOMAIN/enV5/START/sRamValue")
    
    Note over d: GETS MEASUREMENT
    d ->> b: pub("eip://DOMAIN/enV5/DATA/wifiValue")
    b ->> ds: pub("eip://DOMAIN/enV5/DATA/wifiValue")
    ds ->> dt: setWifiValue
    
    Note over d: GETS MEASUREMENT
    d ->> b: pub("eip://DOMAIN/enV5/DATA/sRamValue")
    b ->> ds: pub("eip://DOMAIN/enV5/DATA/sRamValue")
    ds ->> dt: setSRamValue
    
    Note over dt,d: ...
    
    dt ->> ds: publishDataStopRequest
    ds ->> b: pub("eip://DOMAIN/enV5/STOP/wifiValue")
    b ->> d: pub("eip://DOMAIN/enV5/STOP/wifiValue")
    
    dt ->> ds: publishDataStopRequest
    ds ->> b: pub("eip://DOMAIN/enV5/STOP/sRamValue")
    b ->> d: pub("eip://DOMAIN/enV5/STOP/sRamValue")
    d ->> b: unsub("eip://DOMAIN/enV5Twin/STATUS")
```

## Simplified Device Loses Connection

```mermaid
sequenceDiagram
      participant dt as enV5 Twin
      participant b as Broker
      participant d as enV5

    dt ->> b: pub("eip://DOMAIN/enV5Twin/STATUS", ONLINE)
    dt ->> b: sub("eip://DOMAIN/enV5/STATUS")
    
    Note over dt: WAITS FOR DEVICE
    
    d ->> b: sub("eip://DOMAIN/enV5/START/Value")
        
    d ->> b: pub("eip://DOMAIN/enV5/STATUS", ONLINE)
    b ->> dt: pub("eip://DOMAIN/enV5/STATUS", ONLINE)
 
    dt ->> b: pub("eip://DOMAIN/enV5/START/Value")
    b ->> d: pub("eip://DOMAIN/enV5/START/Value")
    d ->> b: sub("eip://DOMAIN/enV5Twin/STATUS")
    
    Note over d: GETS MEASUREMENT
        
    d ->> b: pub("eip://DOMAIN/enV5/DATA/Value")
    b ->> dt: pub("eip://DOMAIN/enV5/DATA/Value")
    
    Note over d: LOSES CONNECTION
        
    b ->> dt:  pub("eip://DOMAIN/enV5/STATUS", OFFLINE)
      
    Note over d: RECONNECTS

    d ->> b: sub("eip://DOMAIN/enV5/START/Value")
        
    d ->> b: pub("eip://DOMAIN/enV5/STATUS", ONLINE)
    b ->> dt: pub("eip://DOMAIN/enV5/STATUS", ONLINE)
 
    dt ->> b: pub("eip://DOMAIN/enV5/START/Value")
    b ->> d: pub("eip://DOMAIN/enV5/START/Value")
    d ->> b: sub("eip://DOMAIN/enV5Twin/STATUS")
    
    Note over d: GETS MEASUREMENT
        
    d ->> b: pub("eip://DOMAIN/enV5/DATA/Value")
    b ->> dt: pub("eip://DOMAIN/enV5/DATA/Value")
```

## Simplified Twin Loses Connection

```mermaid
sequenceDiagram
      participant dt as enV5 Twin
      participant b as Broker
      participant d as enV5

    dt ->> b: pub("eip://DOMAIN/enV5Twin/STATUS", ONLINE)
    dt ->> b: sub("eip://DOMAIN/enV5/STATUS")
    
    Note over dt: WAITS FOR DEVICE
    
    d ->> b: sub("eip://DOMAIN/enV5/START/Value")
        
    d ->> b: pub("eip://DOMAIN/enV5/STATUS", ONLINE)
    b ->> dt: pub("eip://DOMAIN/enV5/STATUS", ONLINE)
 
    dt ->> b: pub("eip://DOMAIN/enV5/START/Value")
    b ->> d: pub("eip://DOMAIN/enV5/START/Value")
    d ->> b: sub("eip://DOMAIN/enV5Twin/STATUS")
    
    Note over d: GETS MEASUREMENT
        
    d ->> b: pub("eip://DOMAIN/enV5/DATA/Value")
    b ->> dt: pub("eip://DOMAIN/enV5/DATA/Value")
    
    Note over dt: LOSES CONNECTION
        
    b ->> d:  pub("eip://DOMAIN/enV5Twin/STATUS", OFFLINE)
        
    d ->> b: unsub("eip://DOMAIN/enV5Twin/STATUS")
            
    Note over d: STOPS PUBLISHING DATA
      
    Note over dt: RECONNECTS


    dt ->> b: pub("eip://DOMAIN/enV5Twin/STATUS", ONLINE)
    dt ->> b: sub("eip://DOMAIN/enV5/STATUS")
    
    b ->> dt: pub("eip://DOMAIN/enV5/STATUS", ONLINE)
 
    dt ->> b: pub("eip://DOMAIN/enV5/START/Value")
    b ->> d: pub("eip://DOMAIN/enV5/START/Value")
    d ->> b: sub("eip://DOMAIN/enV5Twin/STATUS")
    
    Note over d: GETS MEASUREMENT
        
    d ->> b: pub("eip://DOMAIN/enV5/DATA/Value")
    b ->> dt: pub("eip://DOMAIN/enV5/DATA/Value")
```
