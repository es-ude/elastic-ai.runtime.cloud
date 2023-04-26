package org.ude.es.twinImplementations;

import org.ude.es.comm.Status;
import org.ude.es.protocol.DataRequestHandler;
import org.ude.es.protocol.DataRequester;
import org.ude.es.twinBase.ExecutableJavaTwin;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

public class enV5Twin extends ExecutableJavaTwin {

    public static void main(String[] args) throws InterruptedException {
        startJavaTwin(args, "enV5");
    }

    private static final int WAIT_AFTER_COMMAND = 1000;
    private final TwinStub enV5;
    private int bitfilePosition = 0;

    public enV5Twin(String identifier) {
        super(identifier);
        enV5 = new TwinStub(identifier, WAIT_AFTER_COMMAND);
    }

    @Override
    protected void executeOnBind() {
        enV5.bindToCommunicationEndpoint(endpoint);
        setupDeviceStub();
    }

    private void setupDeviceStub() {
        enV5.addWhenDeviceGoesOnline(data ->
                System.out.println("Device " + enV5.getIdentifier() + " online.")
        );
        enV5.addWhenDeviceGoesOnline(this::publishAvailableMeasurements);
        enV5.addWhenDeviceGoesOnline(data -> enV5.waitAfterCommand());

        enV5.addWhenDeviceGoesOffline(data ->
                System.out.println("Device " + enV5.getIdentifier() + " offline.")
        );

        setupFlashCommand();
    }

    private void setupFlashCommand() {
        String cmd = "FLASH";
        subscribeForCommand(cmd, posting -> {
            enV5.publishCommand(cmd, posting.data() + "POSITION:" + bitfilePosition + ";");
            waitForDone(cmd);
        });
    }

    private void waitForDone(String cmd) {
        enV5.subscribeForDone(cmd, posting -> {
            publishDone(cmd, posting.data());
            enV5.unsubscribeFromDone(cmd);
        });
    }

    private void publishAvailableMeasurements(String data) {
        if (!data.contains(Status.Parameter.MEASUREMENTS.getKey()))
            return;

        String measurements = data.substring(data.indexOf(
                Status.Parameter.MEASUREMENTS.getKey()) + Status.Parameter.MEASUREMENTS.getKey().length() + 1);
        measurements = measurements.substring(0, measurements.indexOf(";"));

        this.publishStatus(new Status(minimalStatus).append(Status.Parameter.MEASUREMENTS.value(measurements)));

        for (String measurement : measurements.split(",")) {
            provideValue(measurement);
        }
    }

    void provideValue(String dataID) {
        DataRequester dataRequester = new DataRequester(
                enV5, dataID, this.identifier
        );
        DataRequestHandler dataRequestHandler = new DataRequestHandler(
                this, dataID
        );
        dataRequestHandler.addWhenStartRequestingData(
                dataRequester::startRequestingData
        );
        dataRequestHandler.addWhenStopRequestingData(
                dataRequester::stopRequestingData
        );
        dataRequestHandler.addWhenStopRequestingData(
                enV5::waitAfterCommand
        );
        dataRequester.addWhenNewDataReceived(
                dataRequestHandler::newDataToPublish
        );
    }
        
}
