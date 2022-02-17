package de.ude.es.twin;

import de.ude.es.Checker;
import de.ude.es.comm.Broker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestDigitalTwin {

    private Checker checker;


    @Test
    void canReportItsId() {

        var broker = new Broker("eip://uni-due.de/es");
        var device = new DigitalTwin("/twin1234");
        device.bind(broker);
        assertEquals(
                "eip://uni-due.de/es/twin1234",
                device.ID());
    }

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
        checker.givenDigitalTwin(id);
        checker.givenSubscriptionAtDigitalTwinFor(
                "/DATA/temperature");
        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/DATA/temperature");
        checker.thenPostingIsDelivered();
    }

    @Test
    void subscribingRawDoesNotModifyTopic() {
        checker.givenDigitalTwin("/aTwin");
        checker.givenRawSubscriptionAtDigitalTwinFor("/data");
        checker.whenPostingIsPublishedAtDigitalTwin("/data");
        checker.thenPostingIsNotDelivered();
        checker.givenRawSubscriptionAtDigitalTwinFor("eip://uni-due.de/es/aTwin/data");
        checker.whenPostingIsPublishedAtDigitalTwin("/data");
        checker.thenPostingIsDelivered();
    }

    @Test
    void NoUnsubscribeIfWrongTopic(){
        checker.givenDigitalTwin("/twin1234");
        checker.givenSubscriptionAtDigitalTwinFor("/DATA/temperature");
        checker.givenRawUnsubscriptionAtDigitalTwinFor("eip://uni-due.de/es/DATA/temperature");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/temperature");
        checker.thenPostingIsDelivered();
    }

    @Test
    void subscriberCanUnsubscribe(){
        checker.givenDigitalTwin("/twin1234");

        checker.givenSubscriptionAtDigitalTwinFor("/DATA/temperature");
        checker.givenUnsubscriptionAtDigitalTwinFor("/DATA/temperature");
        checker.whenPostingIsPublishedAtBroker("/twin1234/DATA/temperature");
        checker.thenPostingIsNotDelivered();
    }
}
