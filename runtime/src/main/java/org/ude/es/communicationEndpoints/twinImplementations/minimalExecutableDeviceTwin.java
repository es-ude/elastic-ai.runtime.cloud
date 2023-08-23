package org.ude.es.communicationEndpoints.twinImplementations;

public class minimalExecutableDeviceTwin extends DeviceTwin {

    public minimalExecutableDeviceTwin(String identifier) {
        super(identifier);
    }

    public static void main(String[] args) throws InterruptedException {
        startCommunicationEndpoint(new DeviceTwin("example"), args);
    }

    // Example on how to forward data messages to the Device
    @Override
    protected void executeOnBind() {
        provideValue("exampleValue");
    }
}
