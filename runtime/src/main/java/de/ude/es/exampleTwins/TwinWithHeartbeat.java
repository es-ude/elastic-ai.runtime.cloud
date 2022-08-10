package de.ude.es.exampleTwins;

import de.ude.es.comm.Heartbeater;
import de.ude.es.twin.JavaTwin;
import de.ude.es.util.Timer;

public class TwinWithHeartbeat extends JavaTwin {

    private Heartbeater heartbeater;

    public TwinWithHeartbeat(String identifier) {
        super(identifier);
    }

    public void startHeartbeats(Timer timer, int timePeriodInMs) {
        heartbeater = new Heartbeater(this, identifier, timer, timePeriodInMs);
        heartbeater.start();
    }
    public void stopHeartbeats() {
        heartbeater.stop();
    }

}
