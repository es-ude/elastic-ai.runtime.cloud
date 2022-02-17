package de.ude.es.comm;

import de.ude.es.Checker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestBroker {

    private Checker checker;


    @Test
    void brokerCanPublishPosting() {
        var aBroker = new Broker("eip://uni-due.de/es");
        var topic = "/twin1234/DATA/temperature";
        var posting = new Posting(topic, "13.5");
        aBroker.publish(posting);
    }

    @Test
    void brokerCanReportItsId() {
        var broker = new Broker("eip://uni-due.de/es");
        assertEquals("eip://uni-due.de/es", broker.ID());
    }


    @BeforeEach
    void init() {
        checker = new Checker();
        checker.givenBroker();
    }

    @Test
    void subscriptionCanBeUnsubscribed() {
        checker.givenSubscriptionAtBrokerFor("/twin1234/DATA/a");
        checker.givenUnsubscribeAtBrokerFor("/twin1234/DATA/a");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/a");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void subscriptionDoesNotUnsubscribeWrongTopic(){
        checker.givenSubscriptionAtBrokerFor("/twin1234/DATA/a");
        checker.givenUnsubscribeAtBrokerFor("/DATA/a");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/a");
        checker.thenPostingIsDelivered();
    }
    @Test
    void subscriberCanReceivePostingFromBroker() {
        checker.givenSubscriptionAtBrokerFor("/twin1234/DATA/a");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/a");
        checker.thenPostingIsDelivered();
    }

    @Test
    void subscriberDoesNotReceivePostingWithDifferentTopic() {
        checker.givenSubscriptionAtBrokerFor("/twin1234/DATA/a");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/b");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void longerTopicDoesNotMatch() {
        checker.givenSubscriptionAtBrokerFor("/twin1234/DATA");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/b");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void shorterTopicDoesNotMatch() {
        checker.givenSubscriptionAtBrokerFor("/twin1234/DATA/a");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void subscriberCanUseSingleLevelWildcardInBetween() {
        checker.givenSubscriptionAtBrokerFor("/twin1234/+/a");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/a");
        checker.thenPostingIsDelivered();
    }

    @Test
    void subscriberCanUseSingleLevelWildcardAtEnd() {
        checker.givenSubscriptionAtBrokerFor("/twin1234/DATA/+");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/a");
        checker.thenPostingIsDelivered();
    }

    @Test
    void subscriberCanNotUseSingleLevelWildcardAtEndForShorter() {
        checker.givenSubscriptionAtBrokerFor("/twin1234/DATA/+");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void subscriberCanUseSingleLevelWildcardAtBeginning() {
        checker.givenSubscriptionAtBrokerFor("/+/twin1234/DATA/a");
        checker.whenPostingIsPublishedAtBroker("/XXX/twin1234/DATA/a");
        checker.thenPostingIsDelivered();
    }

}
