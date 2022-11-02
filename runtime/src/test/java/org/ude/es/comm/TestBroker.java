package org.ude.es.comm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.Checker;

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

    @Test
    void singleLevelWildcardDoesNotMatchMultilevels() {
        checker.givenSubscriptionAtBrokerFor("/+/twin1234/DATA/a");
        checker.whenPostingIsPublishedAtBroker("/XXX/yyy/twin1234/DATA/a");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void subscriberCanUseMultiLevelWildcardAtBeginning() {
        checker.givenSubscriptionAtBrokerFor("/#/twin1234/DATA/a");
        checker.whenPostingIsPublishedAtBroker("/xxx/yyyy/y/twin1234/DATA/a");
        checker.thenPostingIsDelivered();
    }

    @Test
    void subscriberCanUseMultiLevelWildcardInMiddle() {
        checker.givenSubscriptionAtBrokerFor("/twin1234/#/DATA/a");
        checker.whenPostingIsPublishedAtBroker("/twin1234/xxx/yyyy/y/DATA/a");
        checker.thenPostingIsDelivered();
    }

    @Test
    void subscriberCanUseMultiLevelWildcardAtEnd() {
        checker.givenSubscriptionAtBrokerFor("/twin1234/DATA/#");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/a/b/c");
        checker.thenPostingIsDelivered();
    }

    @Test
    void subscriptionDoesNotUnsubscribeWrongTopic() {
        checker.givenSubscriptionAtBrokerFor("/twin1234/DATA/a");
        checker.givenUnsubscribeAtBrokerFor("/DATA/a");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/a");
        checker.thenPostingIsDelivered();
    }
}
