package de.ude.es;

import de.ude.es.comm.*;
import de.ude.es.twin.DigitalTwin;
import de.ude.es.twin.TwinWithHeartbeat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
public class IntegrationTest4HeartbeatsAddTwins {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static int PORT;
    private HivemqBroker broker;

    @Container
    public static GenericContainer brokerCont = new GenericContainer(DockerImageName.parse("eclipse-mosquitto:1.6.14"))
            .withExposedPorts(1883);

    @BeforeAll
    static void setUp() {
        PORT = brokerCont.getFirstMappedPort();
    }

    public static class TwinWithHeartbeats {

        private static class DataSubscriber implements Subscriber {
            @Override
            public void deliver(Posting posting) {
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
    }

    @Test
    void SameIDisNoDuplicate() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN, IP, PORT);
        TwinList twinList = new TwinList(0);
        DigitalTwin sink = new DigitalTwin("monitor");
        sink.bind(broker);
        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(twinList);
        heartbeatSubscriber.bind(sink);

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(100);

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(100);

        TwinData twinData0 = new TwinData("Twin 0", "/testTwin0", new MonitorTimer(), 0);
        assertEquals(1, twinList.getTwins().size());
        assertEquals(twinData0.toString(), twinList.getTwins().get(0).toString());

        broker.closeConnection();
    }

    @Test
    void TwinWhoSendHeartbeatGetAdded() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN, IP, PORT);
        TwinList twinList = new TwinList(0);
        DigitalTwin sink = new DigitalTwin("monitor");
        sink.bind(broker);
        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(twinList);
        heartbeatSubscriber.bind(sink);

        assertEquals(0, twinList.getTwins().size());

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(100);
        TwinData twinData0 = new TwinData("Twin 0", "/testTwin0", new MonitorTimer(), 0);
        assertEquals(1, twinList.getTwins().size());
        assertEquals(twinData0.toString(), twinList.getTwins().get(0).toString());

        createTwinWithHeartbeats("testTwin1");
        Thread.sleep(100);
        TwinData twinData1 = new TwinData("Twin 1", "/testTwin1", new MonitorTimer(), 0);
        assertEquals(2, twinList.getTwins().size());
        assertEquals(twinData1.toString(), twinList.getTwins().get(1).toString());

        broker.closeConnection();
    }

    @Test
    void twinGetsKicked() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN, IP, PORT);
        TwinList twinList = new TwinList(300);
        DigitalTwin sink = new DigitalTwin("monitor");
        sink.bind(broker);
        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(twinList);
        heartbeatSubscriber.bind(sink);

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(100);

        TwinData twinData0 = new TwinData("Twin 0", "/testTwin0", new MonitorTimer(), 0);
        assertEquals(1, twinList.getActiveTwins().size());
        assertEquals(twinData0.toString(), twinList.getActiveTwins().get(0).toString());

        Thread.sleep(500);

        assertEquals(0, twinList.getActiveTwins().size());
        broker.closeConnection();
    }

    private TimerMock timer;

    @Test
    void twinGetsReactivated() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN, IP, PORT);
        TwinList twinList = new TwinList(300);
        DigitalTwin sink = new DigitalTwin("monitor");
        sink.bind(broker);
        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(twinList);
        heartbeatSubscriber.bind(sink);

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(100);

        TwinData twinData0 = new TwinData("Twin 0", "/testTwin0", new MonitorTimer(), 0);
        assertEquals(1, twinList.getActiveTwins().size());
        assertEquals(twinData0.toString(), twinList.getActiveTwins().get(0).toString());

        Thread.sleep(500);
        assertEquals(0, twinList.getActiveTwins().size());

        timer.fire();
        Thread.sleep(100);

        assertEquals(1, twinList.getActiveTwins().size());
        assertEquals(twinData0.toString(), twinList.getActiveTwins().get(0).toString());
        broker.closeConnection();
    }

    private void createTwinWithHeartbeats(String identifier) {
        timer = new TimerMock();

        var sink = new TwinWithHeartbeat(identifier);
        sink.bind(broker);
        sink.startHeartbeats(timer, 0);

        var twinWithHeartbeats = new TwinWithHeartbeats();
        twinWithHeartbeats.bind(sink);
    }

}
