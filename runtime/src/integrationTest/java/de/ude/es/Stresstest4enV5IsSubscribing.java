package de.ude.es;

import de.ude.es.comm.HivemqBroker;
import de.ude.es.comm.Posting;
import de.ude.es.twin.JavaTwin;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Thread.sleep;

public class Stresstest4enV5IsSubscribing {

    private static class TestTwin extends JavaTwin {
        public TestTwin(String identifier) {
            super(identifier);
        }

        public void startPublishing() throws InterruptedException {
            int i = 0;
            while (true) {
                this.publishData("stresstestSub", "testData" + i);
                i++;
                sleep(10);
            }
        }
    }

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;

    public static void main(String[] args) throws InterruptedException {
        HivemqBroker broker = new HivemqBroker(DOMAIN, IP, PORT);
        TestTwin twin = new TestTwin("integTestTwin");
        twin.bind(broker);
        twin.startPublishing();
    }
}
