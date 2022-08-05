package de.ude.es.comm;

import de.ude.es.twin.JavaTwin;
import de.ude.es.util.Timeout;
import de.ude.es.util.Timer;

public class Heartbeater {

    private final JavaTwin twin;
    private final String identifier;
    private final Timer timer;
    private final int timePeriodInMs;
    private boolean isSendingHeartbeats = false;

    /**
     * @param identifier     Identifier of the Twin the Heartbeats are sent for.
     * @param timer          The Timer used to implement periodic behaviour.
     * @param timePeriodInMs How long to wait between heartbeats.
     */
    public Heartbeater(JavaTwin twin, String identifier, Timer timer, int timePeriodInMs) {
        this.twin = twin;
        this.identifier = identifier;
        this.timer = timer;
        this.timePeriodInMs = timePeriodInMs;
    }

    /**
     * Sends a heartbeat immediately and every timePeriodInMs thereafter.
     */
    public void start() {
        isSendingHeartbeats = true;
        timer.register(
                this.timePeriodInMs,
                this::timeout);
        twin.publishHeartbeat(identifier);
    }

    public void stop() {
        isSendingHeartbeats = false;
    }

    private void timeout(Timeout timeout) {
        if (isSendingHeartbeats) {
            timeout.restart();
            twin.publishHeartbeat(identifier);
        }
    }

}
