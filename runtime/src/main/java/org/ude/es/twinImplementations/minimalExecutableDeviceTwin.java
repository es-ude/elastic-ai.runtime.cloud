package org.ude.es.twinImplementations;

import org.ude.es.twinBase.DeviceTwin;

import static org.ude.es.twinBase.Executable.startJavaTwin;

public class minimalExecutableDeviceTwin extends DeviceTwin {

    public static void main(String[] args) throws InterruptedException {
        startJavaTwin(new DeviceTwin("example"), args);
    }

    public minimalExecutableDeviceTwin(String identifier) {
        super(identifier);
    }

    public void example() {
        publishData("exampleData", "0.0");
        device.publishDataStartRequest("dataId", this.getDomainAndIdentifier());
    }

}
