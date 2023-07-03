package org.ude.es;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.ude.es.comm.BrokerMock;
import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.JavaTwin;

public class Checker {

    public TestBroker broker;
    public JavaTestTwin javaTwin;
    public SubscriberMock subscriber = new SubscriberMock();
    public Posting expected;
    public List<String> subscriptions = new ArrayList<>();
    public List<String> unsubscribes = new ArrayList<>();
    public final String DOMAIN = "eip://uni-due.de/es";

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

    public void whenPostingIsPublishedAtBroker(
        String topic,
        String data,
        Posting expected
    ) {
        this.expected = expected;
        broker.publish(new Posting(topic, data), false);
    }

    public void whenPostingIsPublishedAtBroker(String topic, String data) {
        String fullTopic = broker.getClientIdentifier() + "/" + topic;
        whenPostingIsPublishedAtBroker(
            topic,
            data,
            new Posting(fullTopic, data)
        );
    }

    //endregion testing with broker

    //region testing with JavaTwin

    public class JavaTestTwin extends JavaTwin {

        public JavaTestTwin(String identifier) {
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
        public void publish(Posting posting, boolean retain) {
            super.publish(posting, retain);
        }
    }

    public void givenJavaTwin(String id) {
        javaTwin = new JavaTestTwin(id);
        javaTwin.bindToCommunicationEndpoint(broker);
    }

    public void givenSubscriptionAtJavaTwinFor(String topic) {
        javaTwin.subscribe(topic, subscriber);
    }

    public void givenUnsubscriptionAtJavaTwinFor(String topic) {
        javaTwin.unsubscribe(topic);
    }

    public void whenPostingIsPublishedAtJavaTwin(String topic) {
        this.whenPostingIsPublishedAtJavaTwin(topic, "");
    }

    public void whenPostingIsPublishedAtJavaTwin(String topic, String data) {
        String fullTopic = javaTwin.getDomainAndIdentifier() + topic;
        expected = new Posting(fullTopic, data);

        Posting posting = new Posting(topic, data);
        javaTwin.publish(posting, false);
    }
    //endregion testing with JavaTwin
}
