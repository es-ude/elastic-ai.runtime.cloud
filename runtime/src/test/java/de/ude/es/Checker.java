package de.ude.es;

import de.ude.es.comm.Broker;
import de.ude.es.comm.Posting;
import de.ude.es.comm.CommunicationEndpoint;
import de.ude.es.comm.Subscriber;
import de.ude.es.twin.DigitalTwin;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class Checker {

    public Broker broker;
    public DigitalTwin twin;
    public SubscriberMock subscriber = new SubscriberMock();

    public Posting expected;
    public List<String> subscriptions = new ArrayList<>();
    public List<String> unsubscribes = new ArrayList<>();


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
                        "for topic "+topic+", topics received:" +
                        getTopics(subscriptions));
    }

    public void thenUnsubscribeIsDoneFor(String topic) {
        assertTrue(unsubscribes.contains(topic),
                "should have received unsubscribe " +
                        "for topic "+topic+", topics received:" +
                        getTopics(unsubscribes));
    }

    private String getTopics(List<String> list) {
        StringBuilder topics = new StringBuilder();
        for(String t : list)
            topics.append(t).append(", ");
        return topics.toString();
    }


    //-- for testing with broker :



    public void givenBroker() {
        broker = new Broker("eip://uni-due.de/es");
    }

    public void givenSubscriptionAtBrokerFor(String topic) {
        givenSubscriptionAtFor(broker, topic);
    }

    public void givenUnsubscribeAtBrokerFor(String topic) {
        givenUnsubscribeAtFor(broker, topic);
    }

    public void whenPostingIsPublishedAtBroker(String topic, String data) {
        whenPostingIsPublishedAt(broker, topic, data);
    }

    public void whenPostingIsPublishedAtBroker(String topic) {
        this.whenPostingIsPublishedAt(broker, topic);
    }


    //-- for testing with digital twin :

    public void givenDigitalTwin(String id) {
        twin = new DigitalTwin(id){
            @Override
            public void subscribe(String topic, Subscriber subscriber) {
                subscriptions.add(topic);
                super.subscribe(topic, subscriber);
            }

            @Override
            public void subscribeRaw(String topic, Subscriber subscriber) {
                subscriptions.add(topic);
                super.subscribeRaw(topic, subscriber);
            }

            @Override
            public void unsubscribeRaw(String topic, Subscriber subscriber) {
                unsubscribes.add(topic);
                super.unsubscribeRaw(topic, subscriber);
            }

        };
        twin.bind(broker);
    }

    public void givenSubscriptionAtDigitalTwinFor(String topic) {
        this.givenSubscriptionAtFor(twin, topic);
    }

    public void givenRawSubscriptionAtDigitalTwinFor(String topic) {
        this.givenRawSubscriptionAtFor(twin, topic);
    }

    public void whenPostingIsPublishedAtDigitalTwin(String topic) {
        whenPostingIsPublishedAt(twin, topic);
    }

    public void givenUnsubscriptionAtDigitalTwinFor(String topic){this.givenUnsubscribeAtFor(twin, topic);}

    public void givenRawUnsubscriptionAtDigitalTwinFor(String topic){this.givenRawUnsubscribeFor(twin, topic);}


    //-- for child classes :

    protected void givenRawUnsubscribeFor(CommunicationEndpoint channel, String topic){
        channel.unsubscribeRaw(topic, subscriber);
    }

    protected void givenRawSubscriptionAtFor(CommunicationEndpoint channel, String topic) {
        channel.subscribeRaw(topic, subscriber);
    }
    protected void givenSubscriptionAtFor(CommunicationEndpoint channel, String topic) {
        channel.subscribe(topic, subscriber);
    }

    protected void givenUnsubscribeAtFor(CommunicationEndpoint channel, String topic) {
        channel.unsubscribe(topic, subscriber);
    }

    protected void whenPostingIsPublishedAt(CommunicationEndpoint channel, String topic, String data) {
        String fullTopic = channel.ID()+topic;
        expected = new Posting(fullTopic, data);

        Posting posting = new Posting(topic, data);
        channel.publish(posting);
    }

    protected void whenPostingIsPublishedAt(CommunicationEndpoint channel, String topic) {
        this.whenPostingIsPublishedAt(channel, topic, "");
    }


}
