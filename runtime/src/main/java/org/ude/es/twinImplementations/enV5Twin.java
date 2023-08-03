package org.ude.es.twinImplementations;

import static org.ude.es.twinBase.Executable.startTwin;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.ude.es.comm.Status;
import org.ude.es.twinBase.DeviceTwin;

public class enV5Twin extends DeviceTwin {

    private int bitfilePosition = 0;
    private String lastStatusMessage = "";

    public enV5Twin(String identifier) {
        super(identifier, 2500);
    }

    public static void main(String[] args) throws InterruptedException {
        startTwin(new enV5Twin("enV5"), args);
    }

    @Override
    protected void executeOnBind() {
        setupDeviceStub();
        setupFlashCommand();
    }

    private void setupDeviceStub() {
        device.addWhenDeviceGoesOnline(data ->
            System.out.println("Device " + device.getIdentifier() + " online.")
        );
        device.addWhenDeviceGoesOnline(this::publishAvailableMeasurements);

        device.addWhenDeviceGoesOffline(data ->
            System.out.println("Device " + device.getIdentifier() + " offline.")
        );
    }

    private void setupFlashCommand() {
        String cmd = "FLASH";
        subscribeForCommand(
            cmd,
            posting -> {
                pauseDataRequests();
                Thread.sleep(2500);
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
                publishDone(cmd, posting.data());
                device.unsubscribeFromDone(cmd);
                resumeDataRequests();
            }
        );
    }

    private void publishAvailableMeasurements(String data) {
        if (Objects.equals(lastStatusMessage, data)) {
            return;
        }
        lastStatusMessage = data;
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

        List<String> measurementValues = Arrays.asList(measurements.split(","));
        for (String value : availableDataRequester.keySet()) {
            if (!measurementValues.contains(value)) {
                removeProvidedValue(value);
            }
        }

        for (String measurement : measurements.split(",")) {
            if (!availableDataRequester.containsKey(measurement)) {
                provideValue(measurement);
            }
        }
    }
}
