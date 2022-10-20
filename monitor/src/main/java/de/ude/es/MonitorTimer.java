package de.ude.es;

import de.ude.es.util.MonitorTimerClient;
import java.util.TimerTask;

public class MonitorTimer implements de.ude.es.util.MonitorTimer {

    private java.util.Timer timer;
    private MonitorTimerClient client;
    private int timeoutMillis;
    private boolean isTimedOut;

    @Override
    public void register(int timeoutMillis, MonitorTimerClient client) {
        this.timeoutMillis = timeoutMillis;
        this.client = client;
        timer = new java.util.Timer();
        start();
    }

    private void start() {
        timer.schedule(
            new TimerTask() {
                @Override
                public void run() {
                    client.timeout();
                    isTimedOut = true;
                }
            },
            timeoutMillis
        );
    }

    @Override
    public void reset() {
        if (!isTimedOut) {
            timer.cancel();
        }
        start();
    }
}
