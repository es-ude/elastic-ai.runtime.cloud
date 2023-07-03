package org.ude.es.twinBase;

import java.util.HashMap;
import org.ude.es.protocol.DataRequestHandler;
import org.ude.es.protocol.DataRequester;

public class DeviceTwin extends JavaTwin {

    protected TwinStub device;
    protected HashMap<String, DataRequester> availableDataRequester =
        new HashMap<>();
    protected HashMap<String, DataRequestHandler> availableDataRequesterHandler =
        new HashMap<>();

    public DeviceTwin(String identifier) {
        super(identifier + "Twin");
        device = new TwinStub(identifier, 1000);
    }

    @Override
    protected void executeOnBindPrivate() {
        super.executeOnBindPrivate();
        device.bindToCommunicationEndpoint(endpoint);
    }

    protected void pauseDataRequests() {
        for (DataRequester dataRequester : availableDataRequester.values()) {
            dataRequester.pauseDataRequests();
        }
    }

    protected void resumeDataRequests() {
        for (DataRequester dataRequester : availableDataRequester.values()) {
            dataRequester.resumeDataRequests();
        }
    }

    protected void removeProvidedValue(String dataID) {
        availableDataRequester.get(dataID).stopRequestingData();
        availableDataRequesterHandler.get(dataID).stop();
        availableDataRequester.remove(dataID);
        availableDataRequesterHandler.remove(dataID);
    }

    protected void provideValue(String dataID) {
        DataRequester dataRequester = new DataRequester(
            device,
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
        dataRequestHandler.addWhenStopRequestingData(device::waitAfterCommand);

        dataRequester.addWhenNewDataReceived(
            dataRequestHandler::newDataToPublish
        );

        availableDataRequester.put(dataID, dataRequester);
        availableDataRequesterHandler.put(dataID, dataRequestHandler);
    }
}
