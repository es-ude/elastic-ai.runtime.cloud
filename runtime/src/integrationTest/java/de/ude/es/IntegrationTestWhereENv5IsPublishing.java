package de.ude.es;

import de.ude.es.comm.HivemqBroker;

/*
 * Note: In order to establish a connection between the ElasticNodeV5 and mosquitto, you need to edit
 * your local mosquitto.conf file and add the following  2 lines:
 *
 * listener 1883 0.0.0.0
 * allow_anonymus true
 *
 * See all broker traffic (ONLY FOR TESTING): mosquitto_sub -t '#'
 */

/**
 * This test corresponds to the ENv5 integration test `hardware-test_MQTTPublish.c` and should receive the String "testData"
 * followed by an increasing number.
 */

public class IntegrationTestWhereENv5IsPublishing {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;

    public static void main(String[] args) {
        HivemqBroker broker = new HivemqBroker(DOMAIN, IP, PORT);
        String TOPIC = "/enV5/DATA/testPub";
        broker.subscribe(TOPIC, posting -> System.out.println(posting.data()));
    }
}
