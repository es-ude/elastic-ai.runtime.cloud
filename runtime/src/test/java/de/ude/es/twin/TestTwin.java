package de.ude.es.twin;

import de.ude.es.Checker;
import de.ude.es.comm.Broker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTwin {

    private Checker checker;

    @BeforeEach
    void init() {
        checker = new Checker();
        checker.givenBroker();
    }

    @Test
    void subscriberCanReceivePostingFromTwin() {
        checkUpdateDelivered("/twin1234");
    }

    @Test
    void twinAddsMissingBackslashToIdentifier() {
        checkUpdateDelivered("twin1234");
    }

    @Test
    void twinRemovesTrailingBackslashFromIdentifier() {
        checkUpdateDelivered("/twin1234/");
    }

    private void checkUpdateDelivered(String id) {
        checker.givenJavaTwin(id);
        checker.givenSubscriptionAtJavaTwinFor(
                "/DATA/temperature");
        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/DATA/temperature");
        checker.thenPostingIsDelivered();
    }

    @Test
    void NoUnsubscribeIfWrongTopic() {
        checker.givenJavaTwin("/twin1234");
        checker.givenSubscriptionAtJavaTwinFor("/DATA/temperature");
        checker.givenUnsubscriptionAtJavaTwinFor("eip://uni-due.de/es/DATA/temperature");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/temperature");
        checker.thenPostingIsDelivered();
    }

    @Test
    void subscriberCanUnsubscribe() {
        checker.givenJavaTwin("/twin1234");

        checker.givenSubscriptionAtJavaTwinFor("/DATA/temperature");
        checker.givenUnsubscriptionAtJavaTwinFor("/DATA/temperature");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/temperature");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void weCanReportId() {
        var broker = new Broker("broker");
        var twin = new Twin("twin");
        twin.bind(broker);
        assertEquals("broker/twin", twin.ID());
    }

}
