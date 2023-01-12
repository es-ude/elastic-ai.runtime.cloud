package org.ude.es.twinImplementations;


import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.Twin;
import org.ude.es.twinBase.TwinStub;


public class enV5Twin extends JavaTwin {

    private static final int WAIT_AFTER_COMMAND = 1000;
    private final TwinStub            enV5;
    public TwinStub.ValueClass wifiValueReceiver;
    public TwinStub.ValueClass sRamValueReceiver;

    public enV5Twin ( String identifier ) {
        super(identifier);
        enV5 = new TwinStub( "enV5", WAIT_AFTER_COMMAND);
        wifiValueReceiver = enV5.newValueClass( "wifiValue");
        sRamValueReceiver = enV5.newValueClass( "sRamValue");
    }

    @Override
    protected void executeOnBind() {
        enV5.bindToCommunicationEndpoint(endpoint);
    }
}
