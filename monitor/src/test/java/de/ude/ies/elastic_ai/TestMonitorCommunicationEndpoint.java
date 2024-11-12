package de.ude.ies.elastic_ai;

import static org.junit.jupiter.api.Assertions.*;

import de.ude.ies.elastic_ai.comm.BrokerMock;
import de.ude.ies.elastic_ai.communicationEndpoints.LocalCommunicationEndpoint;
import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestMonitorCommunicationEndpoint {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String MONITOR_ID = "monitor";
    private static final String DUMMY_ID = "dummy";
    private BrokerMock broker;
    private MonitorCommunicationEndpoint monitorCommunicationEndpoint;
    private LocalCommunicationEndpoint dummyClient;

    @BeforeEach
    void setUp() {
        createBroker();
        createMonitorClient();
        createDummyClient();
    }

    @Test
    void testClientCanReportItsId() {
        assertEquals(
            DOMAIN + "/" + MONITOR_ID,
            monitorCommunicationEndpoint.getDomainAndIdentifier()
        );
    }

    @Test
    void testClientListIsUpdatedOnEnter() {
        assertEquals(1, monitorCommunicationEndpoint.getClientList().getClients().size());
    }

    @Test
    void testClientListIsUpdatedOnLeave() {
        assertTrue(
            monitorCommunicationEndpoint
                .getClientList()
                .getClient(dummyClient.getIdentifier())
                .isActive()
        );
        broker.publish(
            new Posting(
                dummyClient.getIdentifier() + "/STATUS",
                "ID:" + dummyClient.getIdentifier() + ";STATE:OFFLINE;"
            ),
            true
        );
        assertFalse(
            monitorCommunicationEndpoint
                .getClientList()
                .getClient(dummyClient.getIdentifier())
                .isActive()
        );
    }

    @Test
    void testClientListIsUpdatedOnReenter() {
        assertTrue(
            monitorCommunicationEndpoint
                .getClientList()
                .getClient(dummyClient.getIdentifier())
                .isActive()
        );
        broker.publish(
            new Posting(
                dummyClient.getIdentifier() + "/STATUS",
                "ID:" + dummyClient.getIdentifier() + ";STATE:OFFLINE;"
            ),
            true
        );
        assertFalse(
            monitorCommunicationEndpoint
                .getClientList()
                .getClient(dummyClient.getIdentifier())
                .isActive()
        );
        dummyClient.publishStatus(
            new Status().ID(dummyClient.getIdentifier()).STATE(Status.State.ONLINE)
        );
        assertTrue(
            monitorCommunicationEndpoint
                .getClientList()
                .getClient(dummyClient.getIdentifier())
                .isActive()
        );
    }

    private void createBroker() {
        this.broker = new BrokerMock(DOMAIN);
    }

    private void createMonitorClient() {
        monitorCommunicationEndpoint = new MonitorCommunicationEndpoint(MONITOR_ID);
        monitorCommunicationEndpoint.bindToCommunicationEndpoint(broker);
    }

    private void createDummyClient() {
        dummyClient = new LocalCommunicationEndpoint(DUMMY_ID, "localCE");
        dummyClient.bindToCommunicationEndpoint(broker);
    }
}
