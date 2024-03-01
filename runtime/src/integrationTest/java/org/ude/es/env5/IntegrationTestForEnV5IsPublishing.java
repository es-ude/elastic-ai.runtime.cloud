package org.ude.es.env5;

import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;
import org.ude.es.protocol.HivemqBroker;

public class IntegrationTestForEnV5IsPublishing {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;
    private static final String TEST_ID = "integTestTwin";

    public static void main(String[] args) {
        RemoteCommunicationEndpoint endpoint = new RemoteCommunicationEndpoint(
            TEST_ID
        );

        endpoint.bindToCommunicationEndpoint(
            new HivemqBroker(DOMAIN, IP, PORT)
        );
        endpoint.subscribeForData("testPub", System.out::println);
    }
}
