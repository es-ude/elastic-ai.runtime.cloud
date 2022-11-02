package org.ude.es.twinImplementations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ude.es.SubscriberMock;
import org.ude.es.comm.Broker;
import org.ude.es.comm.Posting;
import org.ude.es.comm.PostingType;

public class TestENv5Twin {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String TWIN_ID = "/twin1234";
    private static final String TOPIC = "led1";
    private Broker broker;
    private ENv5TwinStub env5;
    private SubscriberMock subscriber;

    @Test
    void canReportItsId() {
        createBroker();
        createEnv5Twin(TWIN_ID);
        assertEquals(DOMAIN + TWIN_ID, env5.ID());
    }

    @Test
    void canSendMessageToActivateLED() {
        createBroker();
        createEnv5Twin(TWIN_ID);
        createSubscriberFor(TWIN_ID + PostingType.SET.topic(TOPIC));

        env5.activateLED(1);

        var expected = new Posting(
            DOMAIN + TWIN_ID + PostingType.SET.topic(TOPIC),
            "1"
        );
        subscriber.checkPostingDelivered(expected);
    }

    @Test
    void canSendMessageToDeactivateLED() {
        createBroker();
        createEnv5Twin(TWIN_ID);
        createSubscriberFor(TWIN_ID + PostingType.SET.topic(TOPIC));

        env5.deactivateLED(1);

        var expected = new Posting(
            DOMAIN + TWIN_ID + PostingType.SET.topic(TOPIC),
            "0"
        );
        subscriber.checkPostingDelivered(expected);
    }

    private void createBroker() {
        this.broker = new Broker(DOMAIN);
    }

    private void createEnv5Twin(String id) {
        this.env5 = new ENv5TwinStub(id);
        this.env5.bind(this.broker);
    }

    private void createSubscriberFor(String topic) {
        this.subscriber = new SubscriberMock();
        this.broker.subscribe(topic, this.subscriber);
    }
}
