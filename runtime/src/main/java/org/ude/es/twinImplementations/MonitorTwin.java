package org.ude.es.twinImplementations;

import org.ude.es.twinBase.DataRequester;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

public class MonitorTwin extends JavaTwin {
    public MonitorTwin(String identifier) {
        super(identifier);

    }

    @Override
    protected void executeOnBind() {
        TwinStub enV5TwinStub = new TwinStub("enV5Twin");
        enV5TwinStub.bindToCommunicationEndpoint(endpoint);
        DataRequester dataRequester = new DataRequester(enV5TwinStub, "wifiValue", getDomainAndIdentifier());

        // Received new Values
        dataRequester.addWhenNewDataReceived(System.out::println);

        // Start when Value Needed
        dataRequester.startRequestingData();

        // Stop when no longer needed
//        dataRequester.stopRequestingData();
    }
}
