package de.ude.es;

import de.ude.es.comm.HivemqBroker;
import de.ude.es.comm.Posting;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Stresstest4enV5IsSubscribing {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;

    /*
     * Note: In order to establish a connection between the ElasticNodeV5 and mosquitto, you need to edit
     * your local mosquitto.conf file and add the following  2 lines:
     *
     * listener 1883 0.0.0.0
     * allow_anonymous true
     *
     * See all broker traffic (ONLY FOR TESTING): mosquitto_sub -t '#'
     */

    public static void main(String[] args) throws InterruptedException {
        System.out.println("===== \tSTARTING STRESSTEST \t=====");
        HivemqBroker broker = new HivemqBroker(DOMAIN, IP, PORT);
        String TOPIC = "/enV5/DATA/stresstestSub";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
        Date date;

        while (true) {
            date = new Date(System.currentTimeMillis());
            broker.publish(new Posting(TOPIC, "stress on: " + formatter.format(date)));
            Thread.sleep(100);
        }
    }
}
