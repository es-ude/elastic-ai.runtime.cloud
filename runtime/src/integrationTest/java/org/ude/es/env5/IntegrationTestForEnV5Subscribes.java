package org.ude.es.env5;

import org.ude.es.communicationEndpoints.LocalCommunicationEndpoint;
import org.ude.es.protocol.HivemqBroker;

import static java.lang.Thread.sleep;

public class IntegrationTestForEnV5Subscribes {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;
    private static final String TEST_ID = "integTestTwin";

    public static void main(String[] args) throws InterruptedException {

        LocalCommunicationEndpoint endpoint = new LocalCommunicationEndpoint(TEST_ID);
        endpoint.bindToCommunicationEndpoint(new HivemqBroker(DOMAIN, IP, PORT));

        int i = 0;
        while (true) {
            endpoint.publishData("testSub", "testData" + i);
            i++;
            sleep(250);
        }
    }
}
