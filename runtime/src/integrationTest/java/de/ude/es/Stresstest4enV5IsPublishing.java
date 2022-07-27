package de.ude.es;

import de.ude.es.comm.HivemqBroker;
import de.ude.es.twin.JavaTwin;
import de.ude.es.twin.StubTwin;

public class Stresstest4enV5IsPublishing {

    private static class TestTwin extends JavaTwin {
        StubTwin enV5;

        public TestTwin(String identifier) {
            super(identifier);
            enV5 = new StubTwin("enV5");
        }

        public void startTest(String topic) {
            enV5.subscribeForData(topic, posting -> System.out.println(posting.data()));
        }

        @Override
        protected void executeOnBind() {
            super.executeOnBind();
            enV5.bind(endpoint);
        }
    }

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;

    public static void main(String[] args) {
        HivemqBroker broker = new HivemqBroker(DOMAIN, IP, PORT);
        TestTwin twin = new TestTwin("integTestTwin");
        twin.bind(broker);
        twin.startTest("stresstestPub");
    }
}
