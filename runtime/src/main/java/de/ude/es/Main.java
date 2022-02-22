package de.ude.es;

import de.ude.es.comm.HivemqBroker;

public class Main {

    private final static String DOMAIN = "eip://uni-due.de/es";
    private final static int PORT = 1883;
    private static HivemqBroker broker;

    // In mosquitto.conf:
    // listener 1883 0.0.0.0
    // allow_anonymous true
    //
    // See all broker traffic (ONLY FOR TESTING): mosquitto_sub -t '#'
    public static void main(String[] args) {
        broker = new HivemqBroker(DOMAIN, PORT);
        broker.subscribe("/test", posting -> System.out.println(posting.data()));
    }
}
