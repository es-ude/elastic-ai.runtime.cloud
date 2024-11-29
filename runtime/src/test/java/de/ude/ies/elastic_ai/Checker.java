package de.ude.ies.elastic_ai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.ude.ies.elastic_ai.comm.BrokerMock;
import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.Subscriber;
import java.util.ArrayList;
import java.util.List;

public class Checker {

    public final String DOMAIN = "eip://uni-due.de/es";
    public TestBroker broker;
    public LocalCommunicationEndpoint localEndpoint;
    public SubscriberMock subscriber = new SubscriberMock();
    private Posting expected;
    public List<String> subscriptions = new ArrayList<>();
    public List<String> unsubscribes = new ArrayList<>();

    //region testing (non)reception of a posting

    public void thenPostingIsDelivered() {
        subscriber.checkPostingDelivered(expected);
    }

    public void thenPostingIsNotDelivered() {
        subscriber.checkNoPostingDelivered();
    }

    //endregion testing (non)reception of a posting

    //region testing (un)subscription of a topic

    public void thenSubscriptionIsDoneFor(String topic) {
        assertTrue(
            subscriptions.contains(topic),
            "should have received subscription " +
            "for topic " +
            topic +
            ", topics received:" +
            getTopics(subscriptions)
        );
    }

    public void thenUnsubscribeIsDoneFor(String topic) {
        assertTrue(
            unsubscribes.contains(topic),
            "should have received unsubscribe " +
            "for topic " +
            topic +
            ", topics received:" +
            getTopics(unsubscribes)
        );
    }

    private String getTopics(List<String> list) {
        StringBuilder topics = new StringBuilder();
        for (String t : list) topics.append(t).append(", ");
        return topics.toString();
    }

    public void clearPostings() {
        subscriber.clearPostings();
    }

    //endregion testing (un)subscription of a topic

    //region testing with broker

    public void givenBroker() {
        broker = new TestBroker(DOMAIN);
    }

    public void givenSubscriptionAtBrokerFor(String topic) {
        broker.subscribe(topic, subscriber);
    }

    public void givenUnsubscribeAtBrokerFor(String topic) {
        broker.unsubscribe(topic);
    }

    public void whenPostingIsPublishedAtBroker(String topic) {
        whenPostingIsPublishedAtBroker(topic, "");
    }

    private void whenPostingIsPublishedAtBroker(String topic, String data, Posting expected) {
        this.expected = expected;
        broker.publish(new Posting(topic, data), false);
    }

    public void whenPostingIsPublishedAtBroker(String topic, String data) {
        String fullTopic = broker.getClientIdentifier() + "/" + topic;
        whenPostingIsPublishedAtBroker(topic, data, new Posting(fullTopic, data));
    }

    public void givenLocalEndpoint(String id) {
        localEndpoint = new LocalCommunicationEndpoint(id);
        localEndpoint.bindToCommunicationEndpoint(broker);
    }

    //endregion testing with broker

    //region testing with LocalEndpoint

    public void givenSubscriptionAtLocalEndpoint(String topic) {
        localEndpoint.subscribe(topic, subscriber);
    }

    public void givenUnsubscriptionAtLocalEndpointFor(String topic) {
        localEndpoint.unsubscribe(topic);
    }

    public void whenPostingIsPublishedAtLocalEndpoint(String topic) {
        this.whenPostingIsPublishedAtLocalEndpoint(topic, "");
    }

    public void whenPostingIsPublishedAtLocalEndpoint(String topic, String data) {
        String fullTopic = localEndpoint.getDomainAndIdentifier() + topic;
        expected = new Posting(fullTopic, data);

        Posting posting = new Posting(topic, data);
        localEndpoint.publish(posting, false);
    }

    public void isExpecting(Posting posting) {
        expected = posting;
    }

    private class TestBroker extends BrokerMock {

        public TestBroker(String identifier) {
            super(identifier);
        }

        @Override
        public void subscribe(String topic, Subscriber subscriber) {
            subscriptions.add(topic);
            super.subscribe(topic, subscriber);
        }

        @Override
        public void unsubscribe(String topic) {
            unsubscribes.add(topic);
            super.unsubscribe(topic);
        }

        @Override
        public void publish(Posting topic, boolean retain) {
            super.publish(topic, retain);
        }
    }

    public class LocalCommunicationEndpoint
        extends de.ude.ies.elastic_ai.communicationEndpoints.LocalCommunicationEndpoint {

        public LocalCommunicationEndpoint(String identifier) {
            super(identifier, "localCE");
        }

        @Override
        public void subscribe(String topic, Subscriber subscriber) {
            subscriptions.add(topic);
            super.subscribe(topic, subscriber);
        }

        @Override
        public void unsubscribe(String topic) {
            unsubscribes.add(topic);
            super.unsubscribe(topic);
        }

        @Override
        public void publish(Posting posting, boolean retain) {
            super.publish(posting, retain);
        }
    }
    //endregion testing with LocalEndpoint
}
