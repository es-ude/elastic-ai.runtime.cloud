package org.ude.es.twinBase;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.Checker;
import org.ude.es.comm.Posting;
import org.ude.es.comm.Status;
import org.ude.es.twinImplementations.enV5Twin;


public class TestDeviceTwin {

    private static final String deviceID = "test";
    private static final String twinID = deviceID + "Twin";

    private DeviceTwinChecker checker;

    @BeforeEach
    public void setUp() {
        checker = new DeviceTwinChecker();
        checker.givenBroker();
    }

    @Test
    void requestsArePausedAndResumed() {
        DeviceTwin twin = new DeviceTwin(deviceID);
        twin.bindToCommunicationEndpoint(checker.broker);
        checker.givenJavaTwin(deviceID);
        twin.provideValue("value1");

        checker.givenSubscriptionAtBrokerFor(deviceID + "/START/value1");
        checker.whenPostingIsPublishedAtBroker(twinID + "/START/value1");
        checker.isExpecting(new Posting(checker.DOMAIN + "/" + deviceID + "/START/value1", twinID));
        checker.thenPostingIsDelivered();
        checker.clearPostings();

        twin.pauseDataRequests();

        checker.givenSubscriptionAtBrokerFor(deviceID + "/STOP/value1");
        checker.whenPostingIsPublishedAtBroker(twinID + "/STOP/value1");
        checker.isExpecting(new Posting(checker.DOMAIN + "/" + deviceID + "/STOP/value1", twinID));
        checker.thenPostingIsNotDelivered();

        twin.resumeDataRequests();

        checker.thenPostingIsDelivered();
    }

    @Test
    void dataRequestsArePaused() {

    }

    private static class DeviceTwinChecker extends Checker {

        public void whenDevicePublishedStatus(String measurements) {
            javaTwin.publishStatus(new Status(deviceID).append(Status.Parameter.STATE.value(
                            Status.State.ONLINE.get()))
                    .append(Status.Parameter.MEASUREMENTS.value(measurements)));
        }
    }

}
