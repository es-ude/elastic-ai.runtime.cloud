package de.ude.es;

import de.ude.es.comm.HivemqBroker;
import de.ude.es.twin.JavaTwin;

public class Main {

    public static TwinList twinList;
    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final int kikTime = 60000;

    public static void main(String[] args) {
        twinList = new TwinList(kikTime);
        HivemqBroker broker = new HivemqBroker(DOMAIN);

        var sink = new JavaTwin("monitor");
        sink.bind(broker);

        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(
            twinList
        );
        heartbeatSubscriber.bind(broker);

        MonitoringServiceApplication serviceApplication = new MonitoringServiceApplication();
        serviceApplication.startServer(args);
    }
}
