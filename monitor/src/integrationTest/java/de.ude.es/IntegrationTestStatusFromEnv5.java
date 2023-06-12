package de.ude.es;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.ude.es.comm.HivemqBroker;
import org.ude.es.comm.Posting;
import org.ude.es.twinImplementations.IntegrationTestTwinForEnV5;

@Testcontainers
public class IntegrationTestStatusFromEnv5 {

    private int BROKER_PORT = 1883;
    private MonitorTwin monitor;
    private IntegrationTestTwinForEnV5 enV5;

    @Container
    public HiveMQContainer BROKER_CONTAINER = new HiveMQContainer(
        DockerImageName.parse("hivemq/hivemq-ce").withTag("2023.4")
    )
        .withLogLevel(Level.INFO)
        .withExposedPorts(BROKER_PORT)
        .withReuse(false);

    @BeforeEach
    void setUp() {
        BROKER_PORT = BROKER_CONTAINER.getFirstMappedPort();
        createMonitor();
        createEnv5Twin();
    }

    @Test
    void testOnlineCanBeReceived() throws InterruptedException {
        Thread.sleep(1000);
        int activeTwins = monitor.getTwinList().getActiveTwins().size();
        assertEquals(1, activeTwins);
    }

    @Test
    void testOfflineCanBeReceived() throws InterruptedException {
        enV5
            .getEndpoint()
            .publish(
                new Posting(
                    enV5.getIdentifier() + "/STATUS",
                    "ID:" + enV5.getIdentifier() + ";TYPE:TWIN;STATUS:OFFLINE;"
                ),
                true
            );
        Thread.sleep(1000);
        int activeTwins = monitor.getTwinList().getActiveTwins().size();
        assertEquals(0, activeTwins);
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
        String BROKER_IP = "localhost";
        String MQTT_DOMAIN = "eip://uni-due.de/es";
        return new HivemqBroker(MQTT_DOMAIN, BROKER_IP, BROKER_PORT);
    }
}
