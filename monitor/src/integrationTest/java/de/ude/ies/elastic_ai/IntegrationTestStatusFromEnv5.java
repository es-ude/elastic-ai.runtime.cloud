package de.ude.ies.elastic_ai;

import static org.junit.jupiter.api.Assertions.*;

import de.ude.ies.elastic_ai.Clients.ClientData;
import de.ude.ies.elastic_ai.communicationEndpoints.LocalCommunicationEndpoint;
import de.ude.ies.elastic_ai.protocol.HivemqBroker;
import de.ude.ies.elastic_ai.protocol.Posting;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

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
        .withReuse(false)
        .withStartupTimeout(Duration.ofMinutes(3));

    @BeforeEach
    void setUp() {
        BROKER_PORT = BROKER_CONTAINER.getFirstMappedPort();
        createMonitor();
        createEnv5Twin();
    }

    @Test
    void testOnlineCanBeReceived() throws InterruptedException {
        Thread.sleep(1000);
        ClientData client = monitorCommunicationEndpoint
            .getClientList()
            .getClient(enV5Mock.getIdentifier());
        assertTrue(client.isActive());
    }

    @Test
    void testOfflineCanBeReceived() throws InterruptedException {
        enV5Mock
            .getBroker()
            .publish(
                new Posting(
                    enV5Mock.getIdentifier() + "/STATUS",
                    "ID:" + enV5Mock.getIdentifier() + ";STATE:OFFLINE;"
                ),
                true
            );
        Thread.sleep(1000);
        ClientData client = monitorCommunicationEndpoint
            .getClientList()
            .getClient(enV5Mock.getIdentifier());
        assertFalse(client.isActive());
    }

    private void createEnv5Twin() {
        enV5Mock = new LocalCommunicationEndpoint("env5", "localCE");
        enV5Mock.bindToCommunicationEndpoint(createBrokerWithKeepalive("env5"));
    }

    private void createMonitor() {
        monitorCommunicationEndpoint = new MonitorCommunicationEndpoint("monitor");
        monitorCommunicationEndpoint.bindToCommunicationEndpoint(
            createBrokerWithKeepalive("monitor")
        );
    }

    private HivemqBroker createBrokerWithKeepalive(String clientID) {
        String BROKER_IP = "localhost";
        String MQTT_DOMAIN = "eip://uni-due.de/es";
        return new HivemqBroker(MQTT_DOMAIN, BROKER_IP, BROKER_PORT);
    }
}
