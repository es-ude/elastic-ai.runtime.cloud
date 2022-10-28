package org.ude.es.env5;

import org.ude.es.comm.HivemqBroker;
import org.ude.es.twinImplementations.IntegrationTestTwinForEnV5;

public class Stresstest4enV5IsPublishing {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;

    public static void main(String[] args) {
        HivemqBroker broker = new HivemqBroker(DOMAIN, IP, PORT);
        IntegrationTestTwinForEnV5 twin = new IntegrationTestTwinForEnV5(
            "integTestTwin"
        );
        twin.bind(broker);
        twin.startSubscribing("stresstestPub");
    }
}
