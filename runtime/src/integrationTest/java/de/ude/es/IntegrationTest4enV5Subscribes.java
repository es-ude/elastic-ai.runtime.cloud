package de.ude.es;

import de.ude.es.comm.HivemqBroker;
import de.ude.es.twin.JavaTwin;

import static java.lang.Thread.sleep;

public class IntegrationTest4enV5Subscribes {

    private static class TestTwin extends JavaTwin {
        public TestTwin(String identifier) {
            super(identifier);
        }

        public void startPublishing() throws InterruptedException {
            int i = 0;
            while (true) {
                this.publishData("testSub", "testData" + i);
                i++;
                sleep(1000);
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
