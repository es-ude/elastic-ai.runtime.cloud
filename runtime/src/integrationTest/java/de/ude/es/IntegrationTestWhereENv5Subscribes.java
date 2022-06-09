package de.ude.es;

import de.ude.es.comm.HivemqBroker;
import de.ude.es.comm.Posting;

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
 * This test corresponds to the ENv5 integration test `hardware-test_MQTTSubscribe.c` and publishes the String
 * "testData" followed by an increasing number.
 */

public class IntegrationTestWhereENv5Subscribes {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;

    public static void main(String[] args) throws InterruptedException {
        HivemqBroker broker = new HivemqBroker(DOMAIN, IP, PORT);
        String TOPIC = "/enV5/DATA/testSub";
        int i = 0;
        while (true) {
            broker.publish(new Posting(TOPIC, "testData" + i));
            System.out.println("testData" + i);
            Thread.sleep(1000);
            i++;
        }
    }
}
