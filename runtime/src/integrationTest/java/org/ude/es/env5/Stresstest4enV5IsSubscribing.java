package org.ude.es.env5;

import org.ude.es.comm.HivemqBroker;
import org.ude.es.twinImplementations.IntegrationTestTwinForEnV5;

public class Stresstest4enV5IsSubscribing {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;
    private static final String TEST_TWIN_ID = "integTestTwin";

    public static void main(String[] args) throws InterruptedException {
        IntegrationTestTwinForEnV5 twin = new IntegrationTestTwinForEnV5(
            TEST_TWIN_ID
        );
        twin.bindToCommunicationEndpoint(
            new HivemqBroker(DOMAIN, IP, PORT)
        );
        twin.startPublishing(1000);
    }
}
