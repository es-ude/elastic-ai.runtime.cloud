package org.ude.es.twinImplementations;


import org.ude.es.twinBase.DataRequestHandler;
import org.ude.es.twinBase.DataRequester;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

public class enV5Twin extends JavaTwin {

    private static final int WAIT_AFTER_COMMAND = 1000;
    private final TwinStub enV5;

    public enV5Twin(String identifier) {
        super(identifier + "Twin");
        enV5 = new TwinStub(identifier, WAIT_AFTER_COMMAND);
        enV5.addWhenDeviceGoesOnline(() -> System.out.println(identifier + " online."));
        enV5.addWhenDeviceGoesOnline(enV5::waitAfterCommand);
        enV5.addWhenDeviceGoesOffline(() -> System.out.println(identifier + " offline."));
    }

    void provideValue(String dataID) {
        DataRequester dataRequester = new DataRequester(enV5, dataID, this.identifier);
        DataRequestHandler dataRequestHandler = new DataRequestHandler(this, dataID);
        dataRequestHandler.addWhenStartRequestingData(dataRequester::startRequestingData);
        dataRequestHandler.addWhenStopRequestingData(dataRequester::stopRequestingData);
        dataRequester.addWhenNewDataReceived(dataRequestHandler::newDataToPublish);
    }

    @Override
    protected void executeOnBind() {
        enV5.bindToCommunicationEndpoint(endpoint);

        System.out.println(getDomainAndIdentifier());

        provideValue("wifiValue");
        provideValue("sRamValue");
    }
}
