package org.ude.es.twinImplementations;

import org.ude.es.comm.Status;
import org.ude.es.protocol.DataRequestHandler;
import org.ude.es.protocol.DataRequester;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

public class enV5Twin extends JavaTwin {

    private static final int WAIT_AFTER_COMMAND = 1000;
    private final TwinStub enV5;

    public enV5Twin(String identifier) {
        super(identifier + "Twin");

        enV5 = new TwinStub(identifier, WAIT_AFTER_COMMAND);

        enV5.addWhenDeviceGoesOnline(data ->
                System.out.println(identifier + " online.")
        );
        enV5.addWhenDeviceGoesOnline(this::publishAvailableMeasurements);
        enV5.addWhenDeviceGoesOnline(data -> enV5.waitAfterCommand());

        enV5.addWhenDeviceGoesOffline(data ->
                System.out.println(identifier + " offline.")
        );
    }

    private void publishAvailableMeasurements(String data) {
        if (data.contains("MEASUREMENTS:")) {
            System.out.println(data);
            String measurements = data.substring(data.indexOf("MEASUREMENTS:") + 13);
            measurements = measurements.substring(0, measurements.indexOf(";"));
            String[] measurements1 = measurements.split(",");
            this.publishStatus(minimalStatus.append(Status.Parameter.MEASUREMENTS.value(measurements)));
            for (String measurement : measurements1) {
                provideValue(measurement);
            }
        }
    }

    void provideValue(String dataID) {
        DataRequester dataRequester = new DataRequester(
                enV5,
                dataID,
                this.identifier
        );
        DataRequestHandler dataRequestHandler = new DataRequestHandler(
                this,
                dataID
        );
        dataRequestHandler.addWhenStartRequestingData(
                dataRequester::startRequestingData
        );
        dataRequestHandler.addWhenStopRequestingData(
                dataRequester::stopRequestingData
        );
        dataRequestHandler.addWhenStopRequestingData(enV5::waitAfterCommand);
        dataRequester.addWhenNewDataReceived(
                dataRequestHandler::newDataToPublish
        );
    }

    @Override
    protected void executeOnBind() {
        enV5.bindToCommunicationEndpoint(endpoint);
    }
}
