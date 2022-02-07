package de.ude.es;

import de.ude.es.comm.HivemqBroker;

import java.io.IOException;

public class Main {

    static TwinList twinList;
    static HeartbeatSubscriber heartbeatSubscriber;
    static HivemqBroker broker;
    private static final String DOMAIN = "eip://uni-due.de/es";

    public static void main(String[] args) throws IOException {
        twinList = new TwinList();
        broker = new HivemqBroker(DOMAIN);
        heartbeatSubscriber = new HeartbeatSubscriber(broker, twinList);
        MonitoringServiceApplication serviceApplication = new MonitoringServiceApplication();
        serviceApplication.startServer(args);
    }
}
