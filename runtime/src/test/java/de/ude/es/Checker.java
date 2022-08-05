package de.ude.es;

import de.ude.es.comm.Broker;
import de.ude.es.comm.CommunicationEndpoint;
import de.ude.es.comm.Posting;
import de.ude.es.comm.Subscriber;
import de.ude.es.twin.JavaTwin;
import de.ude.es.twin.TwinStub;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class Checker {

    public TestBroker broker;
    public JavaTestTwin javaTwin;
    public TestTwinStub twinStub;
    public SubscriberMock subscriber = new SubscriberMock();

    public Posting expected;
    public List<String> subscriptions = new ArrayList<>();
    public List<String> unsubscribes = new ArrayList<>();

    public final String DOMAIN = "eip://uni-due.de/es";

    //-- for testing (non)reception of a posting :

    public void thenPostingIsDelivered() {
        subscriber.checkPostingDelivered(expected);
    }

    public void thenPostingIsNotDelivered() {
        subscriber.checkNoPostingDelivered();
    }


    //-- for testing (un)subscription of a topic :

    public void thenSubscriptionIsDoneFor(String topic) {
        assertTrue(
                subscriptions.contains(topic),
                "should have received subscription " +
                        "for topic " + topic + ", topics received:" +
                        getTopics(subscriptions));
    }

    public void thenUnsubscribeIsDoneFor(String topic) {
        assertTrue(unsubscribes.contains(topic),
                "should have received unsubscribe " +
                        "for topic " + topic + ", topics received:" +
                        getTopics(unsubscribes));
    }

    private String getTopics(List<String> list) {
        StringBuilder topics = new StringBuilder();
        for (String t : list)
            topics.append(t).append(", ");
        return topics.toString();
    }

    //-- for testing with broker :

    public void givenBroker() {
        broker = new TestBroker(DOMAIN);
    }

    public void givenSubscriptionAtBrokerFor(String topic) {
        broker.subscribe(topic, subscriber);
    }

    public void givenUnsubscribeAtBrokerFor(String topic) {
        broker.unsubscribe(topic, subscriber);
    }

    public void whenPostingIsPublishedAtBroker(String topic) {
        whenPostingIsPublishedAtBroker(topic, "");
    }

    public void whenPostingIsPublishedAtBroker(String topic, String data) {
        String fullTopic = broker.ID() + topic;
        expected = new Posting(fullTopic, data);
        broker.publish(new Posting(topic, data));
    }

    //-- for testing with Java/Stub twin :

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
        public void unsubscribe(String topic, Subscriber subscriber) {
            unsubscribes.add(topic);
            super.unsubscribe(topic, subscriber);
        }

        @Override
        public void publish(Posting topic) {
            super.publish(topic);
        }
    }

    public class TestTwinStub extends TwinStub {

        public TestTwinStub(String identifier) {
            super(identifier);
        }

        @Override
        public void subscribe(String topic, Subscriber subscriber) {
            subscriptions.add(topic);
            super.subscribe(topic, subscriber);
        }

        @Override
        public void unsubscribe(String topic, Subscriber subscriber) {
            unsubscribes.add(topic);
            super.unsubscribe(topic, subscriber);
        }

        @Override
        public void publish(Posting topic) {
            super.publish(topic);
        }
    }

    public class TestBroker extends Broker {

        public TestBroker(String identifier) {
            super(identifier);
        }

        @Override
        public void subscribe(String topic, Subscriber subscriber) {
            subscriptions.add(topic);
            super.subscribe(topic, subscriber);
        }

        @Override
        public void unsubscribe(String topic, Subscriber subscriber) {
            unsubscribes.add(topic);
            super.unsubscribe(topic, subscriber);
        }

        @Override
        public void publish(Posting topic) {
            super.publish(topic);
        }
    }

    public void givenJavaTwin(String id) {
        javaTwin = new JavaTestTwin(id);
        javaTwin.bind(broker);
    }

    public void givenTwinStub(String id) {
        twinStub = new TestTwinStub(id);
        twinStub.bind(broker);
    }

    public void givenSubscriptionAtJavaTwinFor(String topic) {
        javaTwin.subscribe(topic, subscriber);
    }

    public void whenPostingIsPublishedAtDigitalTwin(String topic) {
        this.whenPostingIsPublishedAtDigitalTwin(topic, "");
    }

    public void whenPostingIsPublishedAtDigitalTwin(String topic, String data) {
        String fullTopic = javaTwin.ID() + topic;
        expected = new Posting(fullTopic, data);

        Posting posting = new Posting(topic, data);
        javaTwin.publish(posting);
    }

    //-- for child classes :

//    protected void givenRawUnsubscribeFor(CommunicationEndpoint channel, String topic) {
//        channel.unsubscribeRaw(topic, subscriber);
//    }
//
//    protected void givenRawSubscriptionAtFor(CommunicationEndpoint channel, String topic) {
//        channel.subscribeRaw(topic, subscriber);
//    }
//
//    protected void givenSubscriptionAtFor(CommunicationEndpoint channel, String topic) {
//        channel.subscribe(topic, subscriber);
//    }
//
//    protected void givenUnsubscribeAtFor(CommunicationEndpoint channel, String topic) {
//        channel.unsubscribe(topic, subscriber);
//    }

}
