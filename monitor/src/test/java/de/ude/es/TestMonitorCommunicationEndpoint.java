package de.ude.es;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    private LocalCommunicationEndpoint dummyTwin;

    @BeforeEach
    void setUp() {
        createBroker();
        createMonitorTwin();
        createDummyTwin();
    }

    @Test
    void testTwinCanReportItsId() {
        assertEquals(
            DOMAIN + "/" + MONITOR_ID,
            monitorCommunicationEndpoint.getDomainAndIdentifier()
        );
    }

    @Test
    void testTwinListIsUpdatedOnEnter() {
        assertEquals(
            1,
            monitorCommunicationEndpoint.getTwinList().getTwins().size()
        );
    }

    @Test
    void testTwinListIsUpdatedOnLeave() {
        assertEquals(
            1,
            monitorCommunicationEndpoint.getTwinList().getTwins().size()
        );
        broker.publish(
            new Posting(
                dummyTwin.getIdentifier() + "/STATUS",
                "ID:" + dummyTwin.getIdentifier() + ";TYPE:TWIN;STATE:OFFLINE;"
            ),
            true
        );
        assertEquals(
            0,
            monitorCommunicationEndpoint.getTwinList().getActiveTwins().size()
        );
    }

    @Test
    void testTwinListIsUpdatedOnReenter() {
        assertEquals(
            1,
            monitorCommunicationEndpoint.getTwinList().getTwins().size()
        );
        broker.publish(
            new Posting(
                dummyTwin.getIdentifier() + "/STATUS",
                "ID:" + dummyTwin.getIdentifier() + ";TYPE:TWIN;STATE:OFFLINE;"
            ),
            true
        );
        assertEquals(
            0,
            monitorCommunicationEndpoint.getTwinList().getActiveTwins().size()
        );
        dummyTwin.publishStatus(
            new Status(dummyTwin.getIdentifier())
                .append(Status.Parameter.TYPE.value(Status.Type.TWIN.get()))
                .append(Status.Parameter.STATE.value(Status.State.ONLINE.get()))
        );
        assertEquals(
            1,
            monitorCommunicationEndpoint.getTwinList().getTwins().size()
        );
    }

    private void createBroker() {
        this.broker = new BrokerMock(DOMAIN);
    }

    private void createMonitorTwin() {
        monitorCommunicationEndpoint =
            new MonitorCommunicationEndpoint(MONITOR_ID);
        monitorCommunicationEndpoint.bindToCommunicationEndpoint(broker);
    }

    private void createDummyTwin() {
        dummyTwin = new LocalCommunicationEndpoint(DUMMY_ID);
        dummyTwin.bindToCommunicationEndpoint(broker);
    }
}
