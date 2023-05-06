package org.ude.es.twinImplementations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.Checker;
import org.ude.es.comm.Posting;
import org.ude.es.comm.PostingType;
import org.ude.es.comm.Status;

public class TestEnV5Twin {

    private static final String deviceID = "test";
    private static final String twinID = deviceID + "Twin";

    private EnV5Checker checker;

    private static class EnV5Checker extends Checker {

        public void whenDevicePublishedStatus() {
            expected =
                new Posting(
                    DOMAIN + "/" + twinID + PostingType.STATUS.topic(""),
                    new Status(twinID)
                        .append(
                            Status.Parameter.TYPE.value(Status.Type.TWIN.get())
                        )
                        .append(
                            Status.Parameter.STATE.value(
                                Status.State.ONLINE.get()
                            )
                        )
                        .append(
                            Status.Parameter.MEASUREMENTS.value("value1,value2")
                        )
                        .get()
                );
            javaTwin.publishStatus(
                new Status(deviceID)
                    .append(
                        Status.Parameter.STATE.value(Status.State.ONLINE.get())
                    )
                    .append(
                        Status.Parameter.MEASUREMENTS.value("value1,value2")
                    )
            );
        }

        public void whenFlashIsPublished() {
            expected =
                new Posting(
                    DOMAIN +
                    "/" +
                    deviceID +
                    PostingType.COMMAND.topic("FLASH"),
                    "POSITION:0;"
                );
            whenPostingIsPublishedAtBroker(twinID + "/DO/FLASH", "", expected);
        }
    }

    @BeforeEach
    public void setUp() {
        checker = new EnV5Checker();
        checker.givenBroker();
    }

    @Test
    void statusIncludesMeasurements() {
        enV5Twin twin = new enV5Twin(deviceID);
        twin.bindToCommunicationEndpoint(checker.broker);

        checker.givenJavaTwin(deviceID);
        checker.givenSubscriptionAtBrokerFor(twinID + "/STATUS");
        checker.whenDevicePublishedStatus();
        checker.thenPostingIsDelivered();
    }

    @Test
    void flashCommandIsForwarded() {
        enV5Twin twin = new enV5Twin(deviceID);
        twin.bindToCommunicationEndpoint(checker.broker);

        checker.givenJavaTwin(deviceID);
        checker.givenSubscriptionAtBrokerFor(deviceID + "/DO/FLASH");
        checker.whenFlashIsPublished();
        checker.thenPostingIsDelivered();

        checker.givenSubscriptionAtBrokerFor(twinID + "/DONE/FLASH");
        checker.whenPostingIsPublishedAtBroker(
            deviceID + "/DONE/FLASH",
            "",
            new Posting(checker.DOMAIN + "/" + twinID + "/DONE/FLASH", "")
        );
        checker.thenPostingIsDelivered();
    }
}
