package org.ude.es.communicationEndpoints;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.Checker;
import org.ude.es.protocol.Posting;
import org.ude.es.protocol.PostingType;
import org.ude.es.protocol.Status;

public class TestLocalCommunicationEndpoint {

    private static final String twinID = "test";
    private JavaTwinChecker checker;

    @BeforeEach
    public void setUp() {
        checker = new JavaTwinChecker();
        checker.givenBroker();
        checker.givenDevice();
    }

    @Test
    void weCanPublishData() {
        checker.givenSubscriptionAtBrokerFor(twinID + "/DATA/temperature");
        checker.whenPublishingData("temperature", "13.5");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanPublishStatus() {
        checker.givenSubscriptionAtBrokerFor(twinID + "/STATUS");
        checker.whenPublishingStatus();
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanPublishDone() {
        checker.givenSubscriptionAtBrokerFor(twinID + "/DONE/cmd");
        checker.whenPublishingDone("cmd", "test");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanSubscribeForDataStartRequest() {
        checker.whenSubscribingForDataStart("data");
        checker.whenPostingIsPublishedAtBroker(twinID + "/START/data", twinID);
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromDataStartRequest() {
        checker.whenSubscribingForDataStart("data");
        checker.whenUnsubscribingFromDataStart("data");
        checker.whenPostingIsPublishedAtBroker(twinID + "/START/data", twinID);
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void weCanSubscribeForDataStopRequest() {
        checker.whenSubscribingForDataStop("data");
        checker.whenPostingIsPublishedAtBroker(twinID + "/STOP/data", twinID);
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromDataStopRequest() {
        checker.whenSubscribingForDataStop("data");
        checker.whenUnsubscribingFromDataStop("data");
        checker.whenPostingIsPublishedAtBroker(twinID + "/STOP/data", twinID);
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void weCanSubscribeForCommand() {
        checker.whenSubscribingForCommand("cmd");
        checker.whenPostingIsPublishedAtBroker(twinID + "/DO/cmd", twinID);
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromCommand() {
        checker.whenSubscribingForCommand("data");
        checker.whenUnsubscribingFromCommand("data");
        checker.whenPostingIsPublishedAtBroker(twinID + "/SET/data", twinID);
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void stubIsBound() {
        RemoteCommunicationEndpoint stub = new RemoteCommunicationEndpoint(
            "stub"
        );
        checker.twin.bindStub(stub);
        Assertions.assertNotNull(stub.getBrokerStub());
    }

    private static class JavaTwinChecker extends Checker {

        public LocalCommunicationEndpoint twin;

        public void givenDevice() {
            twin = new LocalCommunicationEndpoint(twinID);
            twin.bindToCommunicationEndpoint(broker);
        }

        public void whenPublishingData(String dataId, String value) {
            String topic =
                twin.getDomainAndIdentifier() + PostingType.DATA.topic(dataId);
            isExpecting(new Posting(topic, value));
            twin.publishData(dataId, value);
        }

        public void whenPublishingDone(String dataId, String value) {
            String topic =
                twin.getDomainAndIdentifier() + PostingType.DONE.topic(dataId);
            isExpecting(new Posting(topic, value));
            twin.publishDone(dataId, value);
        }

        public void whenPublishingStatus() {
            String topic =
                twin.getDomainAndIdentifier() + PostingType.STATUS.topic("");
            isExpecting(
                new Posting(
                    topic,
                    "ID:" + twin.identifier + ";TYPE:TWIN;STATE:ONLINE;"
                )
            );
            twin.publishStatus(
                new Status(twin.getIdentifier())
                    .append(Status.Parameter.TYPE.value(Status.Type.TWIN.get()))
                    .append(
                        Status.Parameter.STATE.value(Status.State.ONLINE.get())
                    )
            );
        }

        public void whenSubscribingForDataStart(String dataId) {
            twin.subscribeForDataStartRequest(dataId, subscriber);
        }

        public void whenUnsubscribingFromDataStart(String dataId) {
            twin.unsubscribeFromDataStartRequest(dataId);
        }

        public void whenSubscribingForDataStop(String dataId) {
            twin.subscribeForDataStopRequest(dataId, subscriber);
        }

        public void whenUnsubscribingFromDataStop(String dataId) {
            twin.unsubscribeFromDataStopRequest(dataId);
        }

        public void whenSubscribingForCommand(String dataId) {
            twin.subscribeForCommand(dataId, subscriber);
        }

        public void whenUnsubscribingFromCommand(String dataId) {
            twin.unsubscribeFromCommand(dataId);
        }
    }
}
