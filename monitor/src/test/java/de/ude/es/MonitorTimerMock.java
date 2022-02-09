package de.ude.es;

import de.ude.es.util.MonitorTimer;
import de.ude.es.util.MonitorTimerClient;

public class MonitorTimerMock implements MonitorTimer {

    private MonitorTimerClient client;

    @Override
    public void register(int timeoutMillis, MonitorTimerClient client) {
        this.client = client;
    }

    public void timeOut() {
        client.timeout();
    }

    @Override
    public void reset() {

    }
}
