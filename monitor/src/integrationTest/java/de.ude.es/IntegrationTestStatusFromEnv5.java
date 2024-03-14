package de.ude.es;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.ude.es.Clients.MonitorCommunicationEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.ude.es.communicationEndpoints.LocalCommunicationEndpoint;
import org.ude.es.protocol.HivemqBroker;
import org.ude.es.protocol.Posting;

@Testcontainers
public class IntegrationTestStatusFromEnv5 {

    private int BROKER_PORT = 1883;
    private MonitorCommunicationEndpoint monitorCommunicationEndpoint;
    private LocalCommunicationEndpoint enV5Mock;

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
            .getClientList()
            .getActiveClients()
            .size();
        assertEquals(1, activeTwins);
    }

    @Test
    void testOfflineCanBeReceived() throws InterruptedException {
        enV5Mock
            .getBrokerStub()
            .publish(
                new Posting(
                    enV5Mock.getIdentifier() + "/STATUS",
                    "ID:" + enV5Mock.getIdentifier() + ";STATUS:OFFLINE;"
                ),
                true
            );
        Thread.sleep(1000);
        int activeTwins = monitorCommunicationEndpoint
            .getClientList()
            .getActiveClients()
            .size();
        assertEquals(0, activeTwins);
    }

    private void createEnv5Twin() {
        enV5Mock = new LocalCommunicationEndpoint("env5", "localCE");
        enV5Mock.bindToCommunicationEndpoint(createBrokerWithKeepalive("env5"));
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
