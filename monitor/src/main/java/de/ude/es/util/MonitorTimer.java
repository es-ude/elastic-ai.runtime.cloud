package de.ude.es.util;

public interface MonitorTimer {
    void register(int timeoutMillis, MonitorTimerClient client);

    void reset();
}
