package de.ude.es;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.twinBase.JavaTwin;

public class TestMonitorTwin {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String MONITOR_ID = "/monitor";
    private static final String DUMMY_ID = "/dummy";
    private BrokerMock broker;
    private MonitorTwin monitorTwin;
    private JavaTwin dummyTwin;

    @BeforeEach
    void setUp() {
        createBroker();
        createMonitorTwin();
        createDummyTwin();
    }

    @Test
    void testTwinCanReportItsId() {
        assertEquals(DOMAIN + MONITOR_ID, monitorTwin.getId());
    }

    @Test
    void testTwinListIsUpdatedOnEnter() {
        assertEquals(0, monitorTwin.getTwinList().getTwins().size());
        dummyTwin.publishStatus(true);
        assertEquals(1, monitorTwin.getTwinList().getTwins().size());
    }

    @Test
    void testTwinListIsUpdatedOnLeave() {
        dummyTwin.publishStatus(true);
        assertEquals(1, monitorTwin.getTwinList().getTwins().size());
        dummyTwin.publishStatus(false);
        assertEquals(0, monitorTwin.getTwinList().getActiveTwins().size());
    }

    @Test
    void testTwinListIsUpdatedOnReenter() {
        dummyTwin.publishStatus(true);
        assertEquals(1, monitorTwin.getTwinList().getTwins().size());
        dummyTwin.publishStatus(false);
        assertEquals(0, monitorTwin.getTwinList().getActiveTwins().size());
        dummyTwin.publishStatus(true);
        assertEquals(1, monitorTwin.getTwinList().getTwins().size());
    }

    private void createBroker() {
        this.broker = new BrokerMock(DOMAIN);
    }

    private void createMonitorTwin() {
        monitorTwin = new MonitorTwin(MONITOR_ID);
        monitorTwin.bindToCommunicationEndpoint(broker);
    }

    private void createDummyTwin() {
        dummyTwin = new JavaTwin(DUMMY_ID);
        dummyTwin.bindToCommunicationEndpoint(broker);
    }
}
