package org.ude.es.twinBase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.Checker;
import org.ude.es.comm.Posting;
import org.ude.es.comm.PostingType;

public class TestTwinStub {

    private static final String twinID = "test";

    private static class TwinStubChecker extends Checker {

        public TwinStub device;

        public void givenDevice() {
            device = new TwinStub("test");
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

        public void whenAskingForDataStart(String data, String receiver) {
            String topic = device.getDomainAndIdentifier() + PostingType.START.topic(data);
            expected = new Posting(topic, receiver);
            device.publishDataStartRequest(data, receiver);
        }

        public void whenAskingForDataStop(String data, String receiver) {
            String topic = device.getDomainAndIdentifier() + PostingType.STOP.topic(data);
            expected = new Posting(topic, receiver);
            device.publishDataStopRequest(data, receiver);
        }

        public void whenSendingCommand(String service, String cmd) {
            String topic = device.getDomainAndIdentifier() + PostingType.COMMAND.topic(service);
            expected = new Posting(topic, cmd);
            device.publishCommand(service, cmd);
        }
    }

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
        checker.whenPostingIsPublishedAtBroker(
                twinID + "/DATA/light",
                "33"
        );
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromData() {
        checker.whenSubscribingForData("light");
        checker.whenUnsubscribingFromData("light");
        checker.whenPostingIsPublishedAtBroker(
                twinID + "/DATA/light",
                "33"
        );
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
}
