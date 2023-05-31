package org.ude.es.twinImplementations;

import org.ude.es.comm.Status;
import org.ude.es.protocol.DataRequester;
import org.ude.es.twinBase.DeviceTwin;

import static org.ude.es.twinBase.Executable.startJavaTwin;

public class enV5Twin extends DeviceTwin {

    public static void main(String[] args) throws InterruptedException {
        startJavaTwin(new DeviceTwin("enV5"), args);
    }

    private int bitfilePosition = 0;

    public enV5Twin(String identifier) {
        super(identifier + "Twin");
    }

    @Override
    protected void executeOnBind() {
        setupDeviceStub();
    }

    private void setupDeviceStub() {
        device.addWhenDeviceGoesOnline(data ->
                System.out.println("Device " + device.getIdentifier() + " online.")
        );
        device.addWhenDeviceGoesOnline(this::publishAvailableMeasurements);
        device.addWhenDeviceGoesOnline(data -> device.waitAfterCommand());

        device.addWhenDeviceGoesOffline(data ->
                System.out.println("Device " + device.getIdentifier() + " offline.")
        );

        setupFlashCommand();
    }

    private void setupFlashCommand() {
        String cmd = "FLASH";
        subscribeForCommand(
                cmd,
                posting -> {
                    blocked = true;
                    for (DataRequester dataRequester : availableDataRequester) {
                        dataRequester.stopRequestingData();
                    }
                    Thread.sleep(2000);
                    device.publishCommand(
                            cmd,
                            posting.data() + "POSITION:" + bitfilePosition + ";"
                    );
                    waitForDone(cmd);
                }
        );
    }

    private void waitForDone(String cmd) {
        device.subscribeForDone(
                cmd,
                posting -> {
                    blocked = false;
                    publishDone(cmd, posting.data());
                    device.unsubscribeFromDone(cmd);
                    for (DataRequester dataRequester : availableDataRequester) {
                        dataRequester.startRequestingData();
                    }
                }
        );
    }

    private void publishAvailableMeasurements(String data) {
        if (!data.contains(Status.Parameter.MEASUREMENTS.getKey())) {
            return;
        }

        String measurements = data.substring(
                data.indexOf(Status.Parameter.MEASUREMENTS.getKey()) +
                        Status.Parameter.MEASUREMENTS.getKey().length() +
                        1
        );
        measurements = measurements.substring(0, measurements.indexOf(";"));

        this.publishStatus(
                new Status(minimalStatus)
                        .append(Status.Parameter.MEASUREMENTS.value(measurements))
        );

        for (String measurement : measurements.split(",")) {
            provideValue(measurement);
        }
    }
}
