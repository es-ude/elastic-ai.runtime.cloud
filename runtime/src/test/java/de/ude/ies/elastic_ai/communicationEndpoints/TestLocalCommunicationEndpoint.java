package de.ude.ies.elastic_ai.communicationEndpoints;

import de.ude.ies.elastic_ai.Checker;
import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.PostingType;
import de.ude.ies.elastic_ai.protocol.Status;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestLocalCommunicationEndpoint {

    private static final String localID = "test";
    private localChecker checker;

    @BeforeEach
    public void setUp() {
        checker = new localChecker();
        checker.givenBroker();
        checker.givenLocalEndpoint();
    }

    @Test
    void weCanPublishData() {
        checker.givenSubscriptionAtBrokerFor(localID + "/DATA/temperature");
        checker.whenPublishingData("temperature", "13.5");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanPublishStatus() {
        checker.givenSubscriptionAtBrokerFor(localID + "/STATUS");
        checker.whenPublishingStatus();
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanPublishDone() {
        checker.givenSubscriptionAtBrokerFor(localID + "/DONE/cmd");
        checker.whenPublishingDone("cmd", "test");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanSubscribeForDataStartRequest() {
        checker.whenSubscribingForDataStart("data");
        checker.whenPostingIsPublishedAtBroker(localID + "/START/data", localID);
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromDataStartRequest() {
        checker.whenSubscribingForDataStart("data");
        checker.whenUnsubscribingFromDataStart("data");
        checker.whenPostingIsPublishedAtBroker(localID + "/START/data", localID);
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void weCanSubscribeForDataStopRequest() {
        checker.whenSubscribingForDataStop("data");
        checker.whenPostingIsPublishedAtBroker(localID + "/STOP/data", localID);
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromDataStopRequest() {
        checker.whenSubscribingForDataStop("data");
        checker.whenUnsubscribingFromDataStop("data");
        checker.whenPostingIsPublishedAtBroker(localID + "/STOP/data", localID);
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void weCanSubscribeForCommand() {
        checker.whenSubscribingForCommand("cmd");
        checker.whenPostingIsPublishedAtBroker(localID + "/DO/cmd", localID);
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromCommand() {
        checker.whenSubscribingForCommand("data");
        checker.whenUnsubscribingFromCommand("data");
        checker.whenPostingIsPublishedAtBroker(localID + "/SET/data", localID);
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void stubIsBound() {
        RemoteCommunicationEndpoint stub = new RemoteCommunicationEndpoint("stub");
        checker.localEndpoint.bindStub(stub);
        Assertions.assertNotNull(stub.getBroker());
    }

    private static class localChecker extends Checker {

        public LocalCommunicationEndpoint localEndpoint;

        public void givenLocalEndpoint() {
            localEndpoint = new LocalCommunicationEndpoint(localID);
            localEndpoint.bindToCommunicationEndpoint(broker);
        }

        public void whenPublishingData(String dataId, String value) {
            String topic = localEndpoint.getDomainAndIdentifier() + PostingType.DATA.topic(dataId);
            isExpecting(new Posting(topic, value));
            localEndpoint.publishData(dataId, value);
        }

        public void whenPublishingDone(String dataId, String value) {
            String topic = localEndpoint.getDomainAndIdentifier() + PostingType.DONE.topic(dataId);
            isExpecting(new Posting(topic, value));
            localEndpoint.publishDone(dataId, value);
        }

        public void whenPublishingStatus() {
            String topic = localEndpoint.getDomainAndIdentifier() + PostingType.STATUS.topic("");
            isExpecting(
                new Posting(topic, "ID:" + localEndpoint.identifier + ";TYPE:NULL;STATE:ONLINE;")
            );
            localEndpoint.publishStatus(
                new Status().ID(localEndpoint.getIdentifier()).STATE(Status.State.ONLINE)
            );
        }

        public void whenSubscribingForDataStart(String dataId) {
            localEndpoint.subscribeForDataStartRequest(dataId, subscriber);
        }

        public void whenUnsubscribingFromDataStart(String dataId) {
            localEndpoint.unsubscribeFromDataStartRequest(dataId);
        }

        public void whenSubscribingForDataStop(String dataId) {
            localEndpoint.subscribeForDataStopRequest(dataId, subscriber);
        }

        public void whenUnsubscribingFromDataStop(String dataId) {
            localEndpoint.unsubscribeFromDataStopRequest(dataId);
        }

        public void whenSubscribingForCommand(String dataId) {
            localEndpoint.subscribeForCommand(dataId, subscriber);
        }

        public void whenUnsubscribingFromCommand(String dataId) {
            localEndpoint.unsubscribeFromCommand(dataId);
        }
    }
}
