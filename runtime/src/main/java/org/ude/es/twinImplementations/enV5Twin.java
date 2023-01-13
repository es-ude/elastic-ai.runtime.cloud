package org.ude.es.twinImplementations;


import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

public class enV5Twin extends JavaTwin {

    private static final int WAIT_AFTER_COMMAND = 1000;
    private final TwinStub enV5;

    public enV5Twin(String identifier) {
        super(identifier + "Twin");
        enV5 = new TwinStub(identifier, new TwinStub.StatusInterface() {
            @Override
            public void deviceGoesOnline() {
                System.out.println(identifier + " online.");
            }

            @Override
            public void deviceGoesOffline() {
                System.out.println(identifier + " offline.");
            }
        }, WAIT_AFTER_COMMAND);

//        sRamValueReceiver = enV5.newDataRequester("sRamValue");
    }

    void provideValue(String dataID) {
        new DataRequestReceiver(dataID, enV5.newDataRequester(dataID, getDomainAndIdentifier()));
    }

    @Override
    protected void executeOnBind() {
        enV5.bindToCommunicationEndpoint(endpoint);

        System.out.println(getDomainAndIdentifier());

//        provideValue("wifiValue");
        provideValue("sRamValue");

    }
}
