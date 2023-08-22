package org.ude.es.communicationEndpoints;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.Checker;
import org.ude.es.protocol.Posting;
import org.ude.es.protocol.PostingType;

public class TestRemoteCommunicationEndpoint {

    private static final String twinID = "test";
    private TwinStubChecker checker;

    @BeforeEach
    public void setUp() {
        checker = new TwinStubChecker();
        checker.givenBroker();
        checker.givenDevice();
    }

    @Test
    void weCanSubscribeForData() {
        checker.whenSubscribingForData("/light");
        checker.whenPostingIsPublishedAtBroker(twinID + "/DATA/light", "33");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromData() {
        checker.whenSubscribingForData("light");
        checker.whenUnsubscribingFromData("light");
        checker.whenPostingIsPublishedAtBroker(twinID + "/DATA/light", "33");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void weCanSubscribeForStatus() {
        checker.whenSubscribingForStatus();
        checker.whenPostingIsPublishedAtBroker(twinID + "/STATUS", "");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromStatus() {
        checker.whenSubscribingForStatus();
        checker.whenUnsubscribingFromStatus();
        checker.whenPostingIsPublishedAtBroker(twinID + "/STATUS", "33");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void weCanSubscribeForDone() {
        checker.whenSubscribingForDone("data");
        checker.whenPostingIsPublishedAtBroker(twinID + "/DONE/data", "");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromDone() {
        checker.whenSubscribingForDone("data");
        checker.whenUnsubscribingFromDone("data");
        checker.whenPostingIsPublishedAtBroker(twinID + "/DONE/data", "33");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void weCanPublishDataStartRequest() {
        checker.givenSubscriptionAtBrokerFor(twinID + "/START/data");
        checker.whenAskingForDataStart("data", "me");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanAskToStopSendingData() {
        checker.givenSubscriptionAtBrokerFor(twinID + "/STOP/data");
        checker.whenAskingForDataStop("data", "me");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanSendACommand() {
        checker.givenSubscriptionAtBrokerFor(twinID + "/DO/led");
        checker.whenSendingCommand("led", "on");
        checker.thenPostingIsDelivered();
    }

    @Test
    void deviceGoesOnline() {
        AtomicReference<Boolean> received = new AtomicReference<>(false);
        checker.device.addWhenDeviceGoesOnline(data -> received.set(true));
        checker.whenPostingIsPublishedAtBroker(
            twinID + "/STATUS",
            "STATUS:ONLINE"
        );
        Assertions.assertTrue(received.get());
    }

    @Test
    void deviceGoesOffline() {
        AtomicReference<Boolean> received = new AtomicReference<>(false);
        checker.device.addWhenDeviceGoesOffline(data -> received.set(true));
        checker.whenPostingIsPublishedAtBroker(
            twinID + "/STATUS",
            "STATUS:OFFLINE"
        );
        Assertions.assertTrue(received.get());
    }

    @Test
    void isOnline() {
        Assertions.assertFalse(checker.device.isOnline());
        checker.whenPostingIsPublishedAtBroker(
            twinID + "/STATUS",
            "STATUS:ONLINE"
        );
        Assertions.assertTrue(checker.device.isOnline());
        checker.whenPostingIsPublishedAtBroker(
            twinID + "/STATUS",
            "STATUS:OFFLINE"
        );
        Assertions.assertFalse(checker.device.isOnline());
    }

    private static class TwinStubChecker extends Checker {

        public RemoteCommunicationEndpoint device;

        public void givenDevice() {
            device = new RemoteCommunicationEndpoint("test");
            device.bindToCommunicationEndpoint(broker);
        }

        public void whenSubscribingForData(String dataId) {
            device.subscribeForData(dataId, subscriber);
        }

        public void whenUnsubscribingFromData(String dataId) {
            device.unsubscribeFromData(dataId);
        }

        public void whenSubscribingForStatus() {
            device.subscribeForStatus(subscriber);
        }

        public void whenUnsubscribingFromStatus() {
            device.unsubscribeFromStatus();
        }

        public void whenSubscribingForDone(String dataId) {
            device.subscribeForDone(dataId, subscriber);
        }

        public void whenUnsubscribingFromDone(String dataId) {
            device.unsubscribeFromDone(dataId);
        }

        public void whenAskingForDataStart(String data, String receiver) {
            String topic =
                device.getDomainAndIdentifier() + PostingType.START.topic(data);
            isExpecting(new Posting(topic, receiver));
            device.publishDataStartRequest(data, receiver);
        }

        public void whenAskingForDataStop(String data, String receiver) {
            String topic =
                device.getDomainAndIdentifier() + PostingType.STOP.topic(data);
            isExpecting(new Posting(topic, receiver));
            device.publishDataStopRequest(data, receiver);
        }

        public void whenSendingCommand(String service, String cmd) {
            String topic =
                device.getDomainAndIdentifier() +
                PostingType.COMMAND.topic(service);
            isExpecting(new Posting(topic, cmd));
            device.publishCommand(service, cmd);
        }
    }
}
