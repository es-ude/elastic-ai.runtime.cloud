package org.ude.es.twinImplementations;


import org.ude.es.twinBase.DeviceTwin;

import static org.ude.es.twinBase.Executable.startTwin;

public class minimalExecutableDeviceTwin extends DeviceTwin {

    public minimalExecutableDeviceTwin(String identifier) {
        super(identifier);
    }

    public static void main(String[] args) throws InterruptedException {
        startTwin(new DeviceTwin("example"), args);
    }

    // Example on how to forward data messages to the Device
    @Override
    protected void executeOnBind() {
        provideValue("exampleValue");
    }
}
