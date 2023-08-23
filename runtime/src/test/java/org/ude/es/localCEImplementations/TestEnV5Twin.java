package org.ude.es.localCEImplementations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.Checker;
import org.ude.es.communicationEndpoints.twinImplementations.enV5Twin;
import org.ude.es.protocol.Posting;
import org.ude.es.protocol.PostingType;
import org.ude.es.protocol.Status;

public class TestEnV5Twin {

    private static final String deviceID = "enV5";
    private static final String twinID = deviceID + "Twin";

    private EnV5Checker checker;

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
        checker.whenDevicePublishedStatus("value1,value2");
        checker.thenPostingIsDelivered();
    }

    @Test
    void sameStatusIsNotPublishedAgain() {
        enV5Twin twin = new enV5Twin(deviceID);
        twin.bindToCommunicationEndpoint(checker.broker);

        checker.givenJavaTwin(deviceID);
        checker.givenSubscriptionAtBrokerFor(twinID + "/STATUS");
        checker.whenDevicePublishedStatus("value1,value2");
        checker.thenPostingIsDelivered();
        checker.clearPostings();
        checker.whenDevicePublishedStatus("value1,value2");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void flashCommandIsForwarded() {
        enV5Twin twin = new enV5Twin(deviceID);
        twin.bindToCommunicationEndpoint(checker.broker);

        checker.givenJavaTwin(deviceID);
        checker.whenDevicePublishedStatus("value1");

        checker.givenSubscriptionAtBrokerFor(deviceID + "/DO/FLASH");
        checker.whenFlashIsPublished();
        checker.thenPostingIsDelivered();
        checker.clearPostings();

        checker.givenSubscriptionAtBrokerFor(deviceID + "/START/value1");
        checker.whenPostingIsPublishedAtBroker(
            twinID + "/START/value1",
            "broker"
        );
        checker.isExpecting(
            new Posting(
                checker.DOMAIN + "/" + deviceID + "/START/value1",
                twinID
            )
        );
        checker.thenPostingIsNotDelivered();

        checker.whenPostingIsPublishedAtBroker(deviceID + "/DONE/FLASH", "");
        checker.isExpecting(
            new Posting(checker.DOMAIN + "/" + twinID + "/DONE/FLASH", "")
        );
        checker.whenPostingIsPublishedAtBroker(
            twinID + "/START/value1",
            "broker"
        );
        checker.isExpecting(
            new Posting(
                checker.DOMAIN + "/" + deviceID + "/START/value1",
                twinID
            )
        );
        checker.thenPostingIsDelivered();
        checker.givenSubscriptionAtBrokerFor(twinID + "/DONE/FLASH");

        checker.thenPostingIsDelivered();
    }

    @Test
    void providedMeasurementsAreChangedWhenDevicesStatusChanges() {
        enV5Twin twin = new enV5Twin(deviceID);
        twin.bindToCommunicationEndpoint(checker.broker);
        checker.givenJavaTwin(deviceID);
        checker.whenDevicePublishedStatus("value1,value2");
        checker.whenDevicePublishedStatus("value2,value3");

        checker.givenSubscriptionAtBrokerFor(deviceID + "/START/value1");
        checker.whenPostingIsPublishedAtBroker(
            twinID + "/START/value1",
            "checker"
        );
        checker.isExpecting(
            new Posting(
                checker.DOMAIN + "/" + deviceID + "/START/value1",
                twinID
            )
        );
        checker.thenPostingIsNotDelivered();

        checker.givenSubscriptionAtBrokerFor(deviceID + "/START/value2");
        checker.whenPostingIsPublishedAtBroker(
            twinID + "/START/value2",
            "checker"
        );
        checker.isExpecting(
            new Posting(
                checker.DOMAIN + "/" + deviceID + "/START/value2",
                twinID
            )
        );
        checker.thenPostingIsDelivered();

        checker.givenSubscriptionAtBrokerFor(deviceID + "/START/value3");
        checker.whenPostingIsPublishedAtBroker(
            twinID + "/START/value3",
            "checker"
        );
        checker.isExpecting(
            new Posting(
                checker.DOMAIN + "/" + deviceID + "/START/value3",
                twinID
            )
        );
        checker.thenPostingIsDelivered();
    }

    private static class EnV5Checker extends Checker {

        public void whenDevicePublishedStatus(String measurements) {
            isExpecting(
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
                            Status.Parameter.MEASUREMENTS.value(measurements)
                        )
                        .get()
                )
            );
            javaTwin.publishStatus(
                new Status(deviceID)
                    .append(
                        Status.Parameter.STATE.value(Status.State.ONLINE.get())
                    )
                    .append(Status.Parameter.MEASUREMENTS.value(measurements))
            );
        }

        public void whenFlashIsPublished() {
            whenPostingIsPublishedAtBroker(twinID + "/DO/FLASH", "");
            isExpecting(
                new Posting(
                    DOMAIN +
                    "/" +
                    deviceID +
                    PostingType.COMMAND.topic("FLASH"),
                    "POSITION:0;"
                )
            );
        }
    }
}
