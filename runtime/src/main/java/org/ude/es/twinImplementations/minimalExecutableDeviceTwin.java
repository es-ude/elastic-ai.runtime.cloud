package org.ude.es.twinImplementations;

import static org.ude.es.twinBase.Executable.startTwin;

import org.ude.es.twinBase.DeviceTwin;

public class minimalExecutableDeviceTwin extends DeviceTwin {

    public static void main(String[] args) throws InterruptedException {
        startTwin(new DeviceTwin("example"), args);
    }

    public minimalExecutableDeviceTwin(String identifier) {
        super(identifier);
    }

    // Example on how to forward data messages to device
    @Override
    protected void executeOnBind() {
        provideValue("exampleValue");
    }
}
