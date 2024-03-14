package de.ude.es;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.ude.es.Clients.MonitorCommunicationEndpoint;
import de.ude.es.comm.BrokerMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.communicationEndpoints.LocalCommunicationEndpoint;
import org.ude.es.protocol.Posting;
import org.ude.es.protocol.Status;

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
        assertEquals(
            1,
            monitorCommunicationEndpoint.getClientList().getClients().size()
        );
    }

    @Test
    void testClientListIsUpdatedOnLeave() {
        assertEquals(
            1,
            monitorCommunicationEndpoint.getClientList().getClients().size()
        );
        broker.publish(
            new Posting(
                dummyClient.getIdentifier() + "/STATUS",
                "ID:" + dummyClient.getIdentifier() + ";STATE:OFFLINE;"
            ),
            true
        );
        assertEquals(
            0,
            monitorCommunicationEndpoint
                .getClientList()
                .getActiveClients()
                .size()
        );
    }

    @Test
    void testClientListIsUpdatedOnReenter() {
        assertEquals(
            1,
            monitorCommunicationEndpoint.getClientList().getClients().size()
        );
        broker.publish(
            new Posting(
                dummyClient.getIdentifier() + "/STATUS",
                "ID:" + dummyClient.getIdentifier() + ";STATE:OFFLINE;"
            ),
            true
        );
        assertEquals(
            0,
            monitorCommunicationEndpoint
                .getClientList()
                .getActiveClients()
                .size()
        );
        dummyClient.publishStatus(
            new Status()
                .ID(dummyClient.getIdentifier())
                .STATE(Status.State.ONLINE)
        );
        assertEquals(
            1,
            monitorCommunicationEndpoint.getClientList().getClients().size()
        );
    }

    private void createBroker() {
        this.broker = new BrokerMock(DOMAIN);
    }

    private void createMonitorClient() {
        monitorCommunicationEndpoint = new MonitorCommunicationEndpoint(
            MONITOR_ID
        );
        monitorCommunicationEndpoint.bindToCommunicationEndpoint(broker);
    }

    private void createDummyClient() {
        dummyClient = new LocalCommunicationEndpoint(DUMMY_ID, "localCE");
        dummyClient.bindToCommunicationEndpoint(broker);
    }
}
