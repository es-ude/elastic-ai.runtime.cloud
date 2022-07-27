package de.ude.es;

import de.ude.es.comm.HivemqBroker;

public class Main {
    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;
    private static HivemqBroker broker;

    // In mosquitto.conf:
    // listener 1883 0.0.0.0
    // allow_anonymous true
    //
    // See all broker traffic (ONLY FOR TESTING): mosquitto_sub -t '#'
    public static void main(String[] args) {
        broker = new HivemqBroker(DOMAIN, IP, PORT);
        broker.subscribe("/test", posting -> System.out.println(posting.data()));
    }
}
