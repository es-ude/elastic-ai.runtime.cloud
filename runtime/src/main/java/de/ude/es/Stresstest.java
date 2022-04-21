package de.ude.es;

import de.ude.es.comm.HivemqBroker;

public class Stresstest {

    private final static String DOMAIN = "eip://uni-due.de/es";
    private final static int PORT = 1883;

    /*
     * Note: In order to establish a connection between the ElasticNodeV5 and mosquitto, you need to edit
     * your local mosquitto.conf file and add the following  2 lines:
     *
     * listener 1883 0.0.0.0
     * allow_anonymous true
     *
     * See all broker traffic (ONLY FOR TESTING): mosquitto_sub -t '#'
     */

    public static void main(String[] args) {
        System.out.println("==== STARTING STRESSTEST =====");
        HivemqBroker broker = new HivemqBroker(DOMAIN, PORT);
        broker.subscribe("/stresstest", posting -> System.out.println(posting.data()));
    }
}
