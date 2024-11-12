package de.ude.ies.elastic_ai.communicationEndpoints;

import de.ude.ies.elastic_ai.Checker;
import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.PostingType;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestRemoteCommunicationEndpoint {

    private static final String remoteID = "test";
    private RemoteStubChecker checker;

    @BeforeEach
    public void setUp() {
        checker = new RemoteStubChecker();
        checker.givenBroker();
        checker.givenDevice();
    }

    @Test
    void weCanSubscribeForData() {
        checker.whenSubscribingForData("/light");
        checker.whenPostingIsPublishedAtBroker(remoteID + "/DATA/light", "33");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromData() {
        checker.whenSubscribingForData("light");
        checker.whenUnsubscribingFromData("light");
        checker.whenPostingIsPublishedAtBroker(remoteID + "/DATA/light", "33");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void weCanSubscribeForStatus() {
        checker.whenSubscribingForStatus();
        checker.whenPostingIsPublishedAtBroker(remoteID + "/STATUS", "");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromStatus() {
        checker.whenSubscribingForStatus();
        checker.whenUnsubscribingFromStatus();
        checker.whenPostingIsPublishedAtBroker(remoteID + "/STATUS", "33");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void weCanSubscribeForDone() {
        checker.whenSubscribingForDone("data");
        checker.whenPostingIsPublishedAtBroker(remoteID + "/DONE/data", "");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromDone() {
        checker.whenSubscribingForDone("data");
        checker.whenUnsubscribingFromDone("data");
        checker.whenPostingIsPublishedAtBroker(remoteID + "/DONE/data", "33");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void weCanPublishDataStartRequest() {
        checker.givenSubscriptionAtBrokerFor(remoteID + "/START/data");
        checker.whenAskingForDataStart("data", "me");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanAskToStopSendingData() {
        checker.givenSubscriptionAtBrokerFor(remoteID + "/STOP/data");
        checker.whenAskingForDataStop("data", "me");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanSendACommand() {
        checker.givenSubscriptionAtBrokerFor(remoteID + "/DO/led");
        checker.whenSendingCommand("led", "on");
        checker.thenPostingIsDelivered();
    }

    @Test
    void deviceGoesOnline() {
        AtomicReference<Boolean> received = new AtomicReference<>(false);
        checker.remoteEndpoint.addWhenDeviceGoesOnline(data -> received.set(true));
        checker.whenPostingIsPublishedAtBroker(remoteID + "/STATUS", "STATUS:ONLINE");
        Assertions.assertTrue(received.get());
    }

    @Test
    void deviceGoesOffline() {
        AtomicReference<Boolean> received = new AtomicReference<>(false);
        checker.remoteEndpoint.addWhenDeviceGoesOffline(data -> received.set(true));
        checker.whenPostingIsPublishedAtBroker(remoteID + "/STATUS", "STATUS:OFFLINE");
        Assertions.assertTrue(received.get());
    }

    @Test
    void isOnline() {
        Assertions.assertFalse(checker.remoteEndpoint.isOnline());
        checker.whenPostingIsPublishedAtBroker(remoteID + "/STATUS", "STATUS:ONLINE");
        Assertions.assertTrue(checker.remoteEndpoint.isOnline());
        checker.whenPostingIsPublishedAtBroker(remoteID + "/STATUS", "STATUS:OFFLINE");
        Assertions.assertFalse(checker.remoteEndpoint.isOnline());
    }

    private static class RemoteStubChecker extends Checker {

        public RemoteCommunicationEndpoint remoteEndpoint;

        public void givenDevice() {
            remoteEndpoint = new RemoteCommunicationEndpoint("test");
            remoteEndpoint.bindToCommunicationEndpoint(broker);
        }

        public void whenSubscribingForData(String dataId) {
            remoteEndpoint.subscribeForData(dataId, subscriber);
        }

        public void whenUnsubscribingFromData(String dataId) {
            remoteEndpoint.unsubscribeFromData(dataId);
        }

        public void whenSubscribingForStatus() {
            remoteEndpoint.subscribeForStatus(subscriber);
        }

        public void whenUnsubscribingFromStatus() {
            remoteEndpoint.unsubscribeFromStatus();
        }

        public void whenSubscribingForDone(String dataId) {
            remoteEndpoint.subscribeForDone(dataId, subscriber);
        }

        public void whenUnsubscribingFromDone(String dataId) {
            remoteEndpoint.unsubscribeFromDone(dataId);
        }

        public void whenAskingForDataStart(String data, String receiver) {
            String topic = remoteEndpoint.getDomainAndIdentifier() + PostingType.START.topic(data);
            isExpecting(new Posting(topic, receiver));
            remoteEndpoint.publishDataStartRequest(data, receiver);
        }

        public void whenAskingForDataStop(String data, String receiver) {
            String topic = remoteEndpoint.getDomainAndIdentifier() + PostingType.STOP.topic(data);
            isExpecting(new Posting(topic, receiver));
            remoteEndpoint.publishDataStopRequest(data, receiver);
        }

        public void whenSendingCommand(String service, String cmd) {
            String topic =
                remoteEndpoint.getDomainAndIdentifier() + PostingType.COMMAND.topic(service);
            isExpecting(new Posting(topic, cmd));
            remoteEndpoint.publishCommand(service, cmd);
        }
    }
}
