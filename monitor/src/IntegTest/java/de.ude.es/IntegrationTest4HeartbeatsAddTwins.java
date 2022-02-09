package de.ude.es;

import de.ude.es.comm.*;
import de.ude.es.twin.DigitalTwin;
import de.ude.es.twin.TwinWithHeartbeat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntegrationTest4HeartbeatsAddTwins {


    private static final String DOMAIN = "eip://uni-due.de/es";
    HivemqBroker broker;


    public static class TwinWithHeartbeats {

        private static class DataSubscriber implements Subscriber {

            public static int heartbeatCount = 0;
            public static String lastData = "";

            @Override
            public void deliver(Posting posting) {
                lastData = posting.data();
                heartbeatCount++;
            }
        }

        private Protocol protocol;
        private TwinWithHeartbeats.DataSubscriber subscriber;

        public void bind(CommunicationEndpoint endpoint) {
            bind(new Protocol(endpoint));
        }

        public void bind(Protocol protocol) {
            this.protocol = protocol;
            this.subscriber = new DataSubscriber();
            this.protocol.subscribeForHeartbeat(DOMAIN + "/+", subscriber);
        }

        public int getHeartbeatCount() {
            return DataSubscriber.heartbeatCount;
        }

        public String getLastData() {
            return DataSubscriber.lastData;
        }

        public void reset() {
            DataSubscriber.heartbeatCount = 0;
            DataSubscriber.lastData = "";
        }

    }

    @Test
    void SameIDisNoDuplicate() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN);
        TwinList twinList = new TwinList();
        DigitalTwin sink = new DigitalTwin("monitor");
        sink.bind(broker);
        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(twinList);
        heartbeatSubscriber.bind(sink);

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(100);

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(100);

        TwinData twinData0 = new TwinData("Twin 0", "/testTwin0");
        assertEquals(1, twinList.getTwins().size());
        assertEquals(twinData0.toString(), twinList.getTwins().get(0).toString());

        broker.closeConnection();
    }

    @Test
    void TwinWhoSendHeartbeatGetAdded() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN);
        TwinList twinList = new TwinList();
        DigitalTwin sink = new DigitalTwin("monitor");
        sink.bind(broker);
        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(twinList);
        heartbeatSubscriber.bind(sink);

        assertEquals(0, twinList.getTwins().size());

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(100);
        TwinData twinData0 = new TwinData("Twin 0", "/testTwin0");
        assertEquals(1, twinList.getTwins().size());
        assertEquals(twinData0.toString(), twinList.getTwins().get(0).toString());

        createTwinWithHeartbeats("testTwin1");
        Thread.sleep(100);
        TwinData twinData1 = new TwinData("Twin 1", "/testTwin1");
        assertEquals(2, twinList.getTwins().size());
        assertEquals(twinData1.toString(), twinList.getTwins().get(1).toString());

        broker.closeConnection();
    }

    private void createTwinWithHeartbeats(String identifier) {
        var sink = new TwinWithHeartbeat(identifier);
        sink.bind(broker);
        sink.startHeartbeats(new TimerMock(), 1000);

        var twinWithHeartbeats = new TwinWithHeartbeats();
        twinWithHeartbeats.bind(sink);

    }

}
