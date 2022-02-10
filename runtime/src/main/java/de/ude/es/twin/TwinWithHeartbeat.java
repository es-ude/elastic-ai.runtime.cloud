package de.ude.es.twin;

import de.ude.es.comm.Heartbeater;
import de.ude.es.comm.Protocol;
import de.ude.es.util.Timer;

public class TwinWithHeartbeat extends DigitalTwin {

    private Heartbeater heartbeater;
    private Protocol protocol;

    public TwinWithHeartbeat(String identifier) {
        super(identifier);
    }

    protected void executeOnBind() {
        protocol = new Protocol(endpoint);
    }

    public void startHeartbeats(Timer timer, int timePeriodInMs) {
        heartbeater = new Heartbeater(protocol, identifier, timer, timePeriodInMs);
        heartbeater.start();
    }

    public void stopHeartbeats() {
        heartbeater.stop();
    }


}
