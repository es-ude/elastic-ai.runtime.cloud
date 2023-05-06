# How To Create Your Own Twin

This guide explains how to create a simple digital twin for your device.

With four steps the minimal setup for a Digital Twin can be done.
After these steps the class should resample [minimalDigitalTwin](../runtime/src/main/java/org/ude/es/twinImplementations/minimalDigitalTwin.java).

1. A Java Class needs to be created, which extends from ExecutableJavaTwin
2. A main method needs to be added, which calls the startJavaTwin in ExecutableJavaTwin
3. A Twin Stub must be created in the initializer (make sure the Device and Twin have different identifiers)
4. Lastly, the Stub needs to be bound to the broker

This minimal Twin on its own has no real functionality.

## Status handler

In the event that the device goes online or offline, functions can be added which take over the status message data and
be executed when the device publishes an online or offline message.

```Java
    void function(String data){
        // ...
    }

    enV5.addWhenDeviceGoesOnline(this::function);

    enV5.addWhenDeviceGoesOffline(this::function);
```

## Data Requester / Data Request Handler

A Data Request Handler can be created to more easily request data from the device on a continuous basis.
The Data Request Handler can be used to process data requests from other twins more easily.

Normally, these two are combined to process the request directed to the twin, which is then forwarded to the device.

To do this, a Data Requester and a Data Request Handler must be created.

```Java
        DataRequester dataRequester = new DataRequester(enV5, dataID, this.identifier);
        DataRequestHandler dataRequestHandler = new DataRequestHandler(this, dataID);
```

The start and stop requests received by the request handler must be forwarded to the data requester.

```Java
        dataRequestHandler.addWhenStartRequestingData(dataRequester::startRequestingData);
        dataRequestHandler.addWhenStopRequestingData(dataRequester::stopRequestingData);
```

When the data requester receives new data, it must be forwarded to the data request handler.

```Java
        dataRequester.addWhenNewDataReceived(dataRequestHandler::newDataToPublish);
```

Optionally, a pause can be inserted to avoid overloading the device with requests.
For this, however, the pause time must be set when initialising the stub.

```Java
        new TwinStub(identifier, [wait time]);

        dataRequestHandler.addWhenStopRequestingData(enV5::waitAfterCommand);
```
