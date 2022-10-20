package de.ude.es;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.ude.es.comm.HivemqBroker;
import de.ude.es.exampleTwins.TwinWithHeartbeat;
import de.ude.es.twin.JavaTwin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class IntegrationTest4HeartbeatsAddTwins {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static int PORT;
    private HivemqBroker broker;

    @Container
    public static GenericContainer brokerCont = new GenericContainer(
        DockerImageName.parse("eclipse-mosquitto:1.6.14")
    )
        .withExposedPorts(1883);

    @BeforeAll
    static void setUp() {
        PORT = brokerCont.getFirstMappedPort();
    }

    @Test
    void SameIDisNoDuplicate() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN, IP, PORT);
        TwinList twinList = new TwinList(0);
        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(
            twinList
        );
        heartbeatSubscriber.bind(broker);

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(100);

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(100);

        assertEquals(1, twinList.getTwins().size());
        TwinData twinData0 = new TwinData(
            "Twin 0",
            "/testTwin0",
            new MonitorTimer(),
            0
        );
        assertEquals(
            twinData0.toString(),
            twinList.getTwins().get(0).toString()
        );

        broker.closeConnection();
    }

    @Test
    void TwinWhoSendHeartbeatGetsAdded() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN, IP, PORT);
        TwinList twinList = new TwinList(0);
        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(
            twinList
        );
        heartbeatSubscriber.bind(broker);

        assertEquals(0, twinList.getTwins().size());

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(50);

        assertEquals(1, twinList.getTwins().size());
        TwinData twinData0 = new TwinData(
            "Twin 0",
            "/testTwin0",
            new MonitorTimer(),
            0
        );
        assertEquals(
            twinData0.toString(),
            twinList.getTwins().get(0).toString()
        );

        createTwinWithHeartbeats("testTwin1");
        Thread.sleep(50);

        assertEquals(2, twinList.getTwins().size());
        TwinData twinData1 = new TwinData(
            "Twin 1",
            "/testTwin1",
            new MonitorTimer(),
            0
        );
        assertEquals(
            twinData1.toString(),
            twinList.getTwins().get(1).toString()
        );

        broker.closeConnection();
    }

    @Test
    void twinGetsKicked() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN, IP, PORT);
        TwinList twinList = new TwinList(300);
        JavaTwin sink = new JavaTwin("monitor");
        sink.bind(broker);
        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(
            twinList
        );
        heartbeatSubscriber.bind(broker);

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(100);

        assertEquals(1, twinList.getActiveTwins().size());
        TwinData twinData0 = new TwinData(
            "Twin 0",
            "/testTwin0",
            new MonitorTimer(),
            0
        );
        assertEquals(
            twinData0.toString(),
            twinList.getActiveTwins().get(0).toString()
        );

        Thread.sleep(500);

        assertEquals(0, twinList.getActiveTwins().size());
        broker.closeConnection();
    }

    private TimerMock timer;

    @Test
    void twinGetsReactivated() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN, IP, PORT);
        TwinList twinList = new TwinList(100);
        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(
            twinList
        );
        heartbeatSubscriber.bind(broker);

        createTwinWithHeartbeats("testTwin0");
        Thread.sleep(50);

        assertEquals(1, twinList.getActiveTwins().size());
        TwinData twinData0 = new TwinData(
            "Twin 0",
            "/testTwin0",
            new MonitorTimer(),
            0
        );
        assertEquals(
            twinData0.toString(),
            twinList.getActiveTwins().get(0).toString()
        );

        Thread.sleep(200);
        assertEquals(0, twinList.getActiveTwins().size());

        timer.fire();
        Thread.sleep(50);

        assertEquals(1, twinList.getActiveTwins().size());
        assertEquals(
            twinData0.toString(),
            twinList.getActiveTwins().get(0).toString()
        );
        broker.closeConnection();
    }

    private void createTwinWithHeartbeats(String identifier) {
        timer = new TimerMock();

        var sink = new TwinWithHeartbeat(identifier);
        sink.bind(broker);
        sink.startHeartbeats(timer, 0);
    }
}
