package de.ude.es;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.ude.es.comm.HivemqBroker;
import org.ude.es.twinImplementations.IntegrationTestTwinForEnV5;

@Testcontainers
public class IntegrationTestStatusFromEnv5 {

    private final String MQTT_DOMAIN = "eip://uni-due.de/es";
    private final String BROKER_IP = "localhost";
    private int BROKER_PORT = 1883;
    private MonitorTwin monitor;
    private IntegrationTestTwinForEnV5 enV5;

    @Container
    public GenericContainer<?> BROKER_CONTAINER = new GenericContainer<>(
        DockerImageName.parse("eclipse-mosquitto:1.6.14")
    )
        .withExposedPorts(BROKER_PORT);

    @BeforeEach
    void setUp() {
        BROKER_PORT = BROKER_CONTAINER.getFirstMappedPort();
        createMonitor();
        createEnv5Twin();
    }

    @Test
    void testOnlineCanBeReceived() throws InterruptedException {
        enV5.publishStatus(true);
        Thread.sleep(1000);
        assertEquals(1, monitor.getTwinList().getActiveTwins().size());
    }

    @Test
    void testOfflineCanBeReceived() throws InterruptedException {
        enV5.publishStatus(false);
        //        while (!monitor.getTwinList().getTwins().isEmpty());
        //        monitor.getTwinList();
        Thread.sleep(1000);
        assertEquals(0, monitor.getTwinList().getActiveTwins().size());
    }

    private void createEnv5Twin() {
        enV5 = new IntegrationTestTwinForEnV5("env5");
        enV5.bindToCommunicationEndpoint(createBrokerWithKeepalive("env5"));
    }

    private void createMonitor() {
        monitor = new MonitorTwin("monitor");
        monitor.bindToCommunicationEndpoint(
            createBrokerWithKeepalive("monitor")
        );
    }

    private HivemqBroker createBrokerWithKeepalive(String clientId) {
        HivemqBroker broker = new HivemqBroker(
            MQTT_DOMAIN,
            BROKER_IP,
            BROKER_PORT,
            clientId
        );
        broker.connectWithKeepaliveAndLwtMessage();
        return broker;
    }
}
