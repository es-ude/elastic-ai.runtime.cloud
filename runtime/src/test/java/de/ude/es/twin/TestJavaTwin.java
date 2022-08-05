package de.ude.es.twin;

import de.ude.es.Checker;
import de.ude.es.comm.Posting;
import de.ude.es.comm.PostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestJavaTwin {

    private static final String twinID = "test";

    private static class JavaTwinChecker extends Checker {
        public JavaTwin device;

        public void givenDevice() {
            device = new JavaTwin(twinID);
            device.bind(broker);
        }

        public void whenPublishingData(String dataId, String value) {
            String topic = device.ID() + PostingType.DATA.topic(dataId);
            expected = new Posting(topic, value);
            device.publishData(dataId, value);
        }

        public void whenPublishingHeartbeat(String who) {
            String topic = device.ID() + PostingType.HEARTBEAT.topic("");
            expected = new Posting(topic, who);
            device.publishHeartbeat(who);
        }

        public void whenSubscribingForDataStart(String dataId) {
            device.subscribeForDataStartRequest(dataId, subscriber);
        }

    }

    private JavaTwinChecker checker;

    @BeforeEach
    public void setUp() {
        checker = new JavaTwinChecker();
        checker.givenBroker();
        checker.givenDevice();
    }

    @Test
    void weCanPublishData() {
        checker.givenSubscriptionAtBrokerFor("/" + twinID + "/DATA/temperature");
        checker.whenPublishingData("temperature", "13.5");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanPublishHeartbeat() {
        checker.givenSubscriptionAtBrokerFor("/" + twinID + "/HEART");
        checker.whenPublishingHeartbeat(twinID);
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanSubscribeForDataStartRequest() {
        checker.whenSubscribingForDataStart("data");
        checker.whenPostingIsPublishedAtBroker("/" + twinID + "/START/data", twinID);
        checker.thenPostingIsDelivered();
    }

}
