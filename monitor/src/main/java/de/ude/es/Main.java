package de.ude.es;

import java.io.IOException;

public class Main {

    static TwinList twinList;

    public static void main(String[] args) throws IOException {
        twinList = new TwinList();
        MonitoringServiceApplication serviceApplication = new MonitoringServiceApplication();
        serviceApplication.startServer(args);
    }
}
