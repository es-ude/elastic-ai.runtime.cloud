package de.ude.ies.elastic_ai.env5;

import static java.lang.Thread.sleep;

import de.ude.ies.elastic_ai.communicationEndpoints.LocalCommunicationEndpoint;
import de.ude.ies.elastic_ai.protocol.HivemqBroker;

public class StressTestForEnV5IsSubscribing {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;
    private static final String TEST_ID = "enV5";

    public static void main(String[] args) throws InterruptedException {
        LocalCommunicationEndpoint endpoint = new LocalCommunicationEndpoint(TEST_ID, "localCE");
        endpoint.bindToCommunicationEndpoint(new HivemqBroker(DOMAIN, IP, PORT));

        int i = 0;
        while (true) {
            endpoint.publishData("testSub", "testData" + i);
            i++;
            sleep(10);
        }
    }
}
