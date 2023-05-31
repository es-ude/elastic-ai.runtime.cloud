package org.ude.es.twinBase;

import org.ude.es.protocol.DataRequestHandler;
import org.ude.es.protocol.DataRequester;

import java.util.ArrayList;

public class DeviceTwin extends JavaTwin {

    protected TwinStub device;
    protected ArrayList<DataRequester> availableDataRequester = new ArrayList<>();
    protected boolean blocked = false;

    private void blockDataStartRequests() {
        while (blocked) {
            //Just blocking Requests
        }
    }

    public DeviceTwin(String identifier) {
        super(identifier);
        device = new TwinStub(identifier, 1000);
    }

    @Override
    protected void executeOnBindPrivate() {
        super.executeOnBindPrivate();
        device.bindToCommunicationEndpoint(endpoint);
    }

    protected void provideValue(String dataID) {
        DataRequester dataRequester = new DataRequester(
                device,
                dataID,
                this.identifier
        );
        availableDataRequester.add(dataRequester);
        DataRequestHandler dataRequestHandler = new DataRequestHandler(
                this,
                dataID
        );

        dataRequestHandler.addWhenStartRequestingData(
                this::blockDataStartRequests
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
    }
}
