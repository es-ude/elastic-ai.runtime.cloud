package de.ude.es;

import java.io.IOException;

public class Main {

    static MonitoringTwin twin = new MonitoringTwin();

    public static void main(String[] args) throws IOException {
        MonitoringServiceApplication serviceApplication = new MonitoringServiceApplication();

        serviceApplication.startServer(args);
    }
}
