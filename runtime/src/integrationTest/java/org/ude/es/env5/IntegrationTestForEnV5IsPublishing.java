package org.ude.es.env5;

import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;
import org.ude.es.protocol.HivemqBroker;

public class IntegrationTestForEnV5IsPublishing {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "192.168.203.18";
    private static final int PORT = 1883;
    private static final String TEST_ID = "enV5";

    public static void main(String[] args) {
        RemoteCommunicationEndpoint endpoint = new RemoteCommunicationEndpoint(
            TEST_ID
        );

        HivemqBroker broker = new HivemqBroker(DOMAIN, IP, PORT);

        endpoint.bindToCommunicationEndpoint(
            broker
        );
        endpoint.subscribeForData("testPub", posting -> System.out.println(posting.data()));
    }
}
