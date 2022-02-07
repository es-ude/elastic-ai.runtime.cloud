package de.ude.es;

import de.ude.es.comm.HivemqBroker;
import de.ude.es.twin.DigitalTwin;

import java.io.IOException;

public class Main {

    public static TwinList twinList;
    private static final String DOMAIN = "eip://uni-due.de/es";

    public static void main(String[] args) throws IOException {
        twinList = new TwinList();
        HivemqBroker broker = new HivemqBroker(DOMAIN);

        var sink = new DigitalTwin("monitor");
        sink.bind(broker);
        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(twinList);
        heartbeatSubscriber.bind(sink);

        MonitoringServiceApplication serviceApplication = new MonitoringServiceApplication();
        serviceApplication.startServer(args);
    }
}