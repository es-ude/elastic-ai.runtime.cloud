package de.ude.es;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.ude.es.communicationEndpoints.twinImplementations.IntegrationTestTwinForEnV5;
import org.ude.es.protocol.HivemqBroker;
import org.ude.es.protocol.Posting;

@Testcontainers
public class IntegrationTestStatusFromEnv5 {

    private int BROKER_PORT = 1883;
    private MonitorCommunicationEndpoint monitorCommunicationEndpoint;
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
        int activeTwins = monitorCommunicationEndpoint
            .getTwinList()
            .getActiveTwins()
            .size();
        assertEquals(1, activeTwins);
    }

    @Test
    void testOfflineCanBeReceived() throws InterruptedException {
        enV5
            .getBrokerStub()
            .publish(
                new Posting(
                    enV5.getIdentifier() + "/STATUS",
                    "ID:" + enV5.getIdentifier() + ";TYPE:TWIN;STATUS:OFFLINE;"
                ),
                true
            );
        Thread.sleep(1000);
        int activeTwins = monitorCommunicationEndpoint
            .getTwinList()
            .getActiveTwins()
            .size();
        assertEquals(0, activeTwins);
    }

    private void createEnv5Twin() {
        enV5 = new IntegrationTestTwinForEnV5("env5");
        enV5.bindToCommunicationEndpoint(createBrokerWithKeepalive("env5"));
    }

    private void createMonitor() {
        monitorCommunicationEndpoint = new MonitorCommunicationEndpoint(
            "monitor"
        );
        monitorCommunicationEndpoint.bindToCommunicationEndpoint(
            createBrokerWithKeepalive("monitor")
        );
    }

    private HivemqBroker createBrokerWithKeepalive(String clientId) {
        String BROKER_IP = "localhost";
        String MQTT_DOMAIN = "eip://uni-due.de/es";
        return new HivemqBroker(MQTT_DOMAIN, BROKER_IP, BROKER_PORT);
    }
}
