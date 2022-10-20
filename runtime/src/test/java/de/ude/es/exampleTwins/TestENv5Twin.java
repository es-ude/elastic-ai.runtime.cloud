package de.ude.es.exampleTwins;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.ude.es.SubscriberMock;
import de.ude.es.comm.Broker;
import de.ude.es.comm.Posting;
import org.junit.jupiter.api.Test;

public class TestENv5Twin {

    private Broker broker;
    private ENv5TwinStub env5;
    private SubscriberMock subscriber;

    @Test
    void canReportItsId() {
        broker = new Broker("eip://uni-due.de/es");
        env5 = new ENv5TwinStub("/twin1234");
        env5.bind(broker);
        assertEquals("eip://uni-due.de/es/twin1234", env5.ID());
    }

    @Test
    void canSendMessageToActivateLED() {
        createBroker();
        env5 = createEnv5Twin("/twin1234");
        subscriber = createSubscriberFor("/twin1234/SET/led1");

        env5.activateLED(1);

        var expected = new Posting(
            "eip://uni-due.de/es/twin1234/SET/led1",
            "1"
        );
        subscriber.checkPostingDelivered(expected);
    }

    @Test
    void canSendMessageToDeactivateLED() {
        createBroker();
        env5 = createEnv5Twin("/twin1234");
        subscriber = createSubscriberFor("/twin1234/SET/led1");

        env5.deactivateLED(1);

        var expected = new Posting(
            "eip://uni-due.de/es/twin1234/SET/led1",
            "0"
        );
        subscriber.checkPostingDelivered(expected);
    }

    private Broker createBroker() {
        broker = new Broker("eip://uni-due.de/es");
        return broker;
    }

    private ENv5TwinStub createEnv5Twin(String id) {
        env5 = new ENv5TwinStub(id);
        env5.bind(broker);
        return env5;
    }

    private SubscriberMock createSubscriberFor(String topic) {
        subscriber = new SubscriberMock();
        broker.subscribe(topic, subscriber);
        return subscriber;
    }
}
