package org.ude.es.env5;

import org.ude.es.comm.HivemqBroker;
import org.ude.es.twinImplementations.IntegrationTestTwinForEnV5;

public class IntegrationTest4enV5Subscribes {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;

    public static void main(String[] args) throws InterruptedException {
        HivemqBroker broker = new HivemqBroker(DOMAIN, IP, PORT);
        IntegrationTestTwinForEnV5 twin = new IntegrationTestTwinForEnV5(
            "integTestTwin"
        );
        twin.bind(broker);
        twin.startPublishing(250);
    }
}
