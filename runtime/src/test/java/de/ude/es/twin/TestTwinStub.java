package de.ude.es.twin;


import de.ude.es.Checker;
import de.ude.es.comm.Posting;
import de.ude.es.comm.PostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class TestTwinStub {

    private static final String twinID = "test";

    private static class TwinStubChecker extends Checker {

        public TwinStub device;

        public void givenDevice () {
            device = new TwinStub( "test" );
            device.bind( broker );
        }

        public void whenSubscribingForData ( String dataId ) {
            device.subscribeForData( dataId, subscriber );
        }

        public void whenUnsubscribingFromData ( String dataId ) {
            device.unsubscribeFromData( dataId, subscriber );
        }

        public void whenSubscribingForStatus () {
            device.subscribeForStatus( subscriber );
        }

        public void whenUnsubscribingFromStatus () {
            device.unsubscribeFromStatus( subscriber );
        }

        public void whenSubscribingForLost () {
            device.subscribeForLost( subscriber );
        }

        public void whenUnsubscribingFromLost () {
            device.unsubscribeFromLost( subscriber );
        }

        public void whenAskingForDataStart ( String data, String receiver ) {
            String topic = device.ID( ) + PostingType.START.topic( data );
            expected = new Posting( topic, receiver );
            device.publishDataStartRequest( data, receiver );
        }

        public void whenAskingForDataStop ( String data, String receiver ) {
            String topic = device.ID( ) + PostingType.STOP.topic( data );
            expected = new Posting( topic, receiver );
            device.publishDataStopRequest( data, receiver );
        }

        public void whenSendingCommand ( String service, String cmd ) {
            String topic = device.ID( ) + PostingType.SET.topic( service );
            expected = new Posting( topic, cmd );
            device.publishCommand( service, cmd );
        }

        public void whenSendingOnCommand ( String service ) {
            String topic = device.ID( ) + PostingType.SET.topic( service );
            expected = new Posting( topic, "1" );
            device.publishOnCommand( service );
        }

        public void whenSendingOffCommand ( String service ) {
            String topic = device.ID( ) + PostingType.SET.topic( service );
            expected = new Posting( topic, "0" );
            device.publishOffCommand( service );
        }
    }

    private TwinStubChecker checker;

    @BeforeEach
    public void setUp () {
        checker = new TwinStubChecker( );
        checker.givenBroker( );
        checker.givenDevice( );
    }

    @Test
<<<<<<< HEAD
    void weCanSubscribeForData () {
        checker.whenSubscribingForData( "/light" );
        checker.whenPostingIsPublishedAtBroker( "/" + twinID + "/DATA/light",
                                                "33" );
        checker.thenPostingIsDelivered( );
    }

    @Test
    void weCanUnsubscribeFromData () {
        checker.whenSubscribingForData( "/light" );
        checker.whenUnsubscribingFromData( "/light" );
        checker.whenPostingIsPublishedAtBroker( "/" + twinID + "/DATA/light",
                                                "33" );
        checker.thenPostingIsNotDelivered( );
=======
    void weCanSubscribeForData() {
        checker.whenSubscribingForData("/light");
        checker.whenPostingIsPublishedAtBroker(
            "/" + twinID + "/DATA/light",
            "33"
        );
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeFromData() {
        checker.whenSubscribingForData("/light");
        checker.whenUnsubscribingFromData("/light");
        checker.whenPostingIsPublishedAtBroker(
            "/" + twinID + "/DATA/light",
            "33"
        );
        checker.thenPostingIsNotDelivered();
>>>>>>> main
    }

    @Test
    void weCanSubscribeForStatus () {
        checker.whenSubscribingForStatus( );
        checker.whenPostingIsPublishedAtBroker( "/" + twinID + "/STATUS", "" );
        checker.thenPostingIsDelivered( );
    }

    @Test
<<<<<<< HEAD
    void weCanUnsubscribeFromStatus () {
        checker.whenSubscribingForStatus( );
        checker.whenUnsubscribingFromStatus( );
        checker.whenPostingIsPublishedAtBroker( "/" + twinID + "/STATUS",
                                                "33" );
        checker.thenPostingIsNotDelivered( );
=======
    void weCanUnsubscribeFromHeartbeat() {
        checker.whenSubscribingForHeartbeat();
        checker.whenUnsubscribingFromHeartbeat();
        checker.whenPostingIsPublishedAtBroker(
            "/" + twinID + "/HEART/light",
            "33"
        );
        checker.thenPostingIsNotDelivered();
>>>>>>> main
    }

    @Test
    void weCanSubscribeForLost () {
        checker.whenSubscribingForLost( );
        checker.whenPostingIsPublishedAtBroker( "/" + twinID + "/LOST", "33" );
        checker.thenPostingIsDelivered( );
    }

    @Test
    void weCanUnsubscribeFromLost () {
        checker.whenSubscribingForLost( );
        checker.whenUnsubscribingFromLost( );
        checker.whenPostingIsPublishedAtBroker( "/" + twinID + "/LOST", "33" );
        checker.thenPostingIsNotDelivered( );
    }

    @Test
    void weCanPublishDataStartRequest () {
        checker.givenSubscriptionAtBrokerFor( "/" + twinID + "/START/data" );
        checker.whenAskingForDataStart( "data", "me" );
        checker.thenPostingIsDelivered( );
    }

    @Test
    void weCanAskToStopSendingData () {
        checker.givenSubscriptionAtBrokerFor( "/" + twinID + "/STOP/data" );
        checker.whenAskingForDataStop( "data", "me" );
        checker.thenPostingIsDelivered( );
    }

    @Test
    void weCanSendACommand () {
        checker.givenSubscriptionAtBrokerFor( "/" + twinID + "/SET/led" );
        checker.whenSendingCommand( "led", "on" );
        checker.thenPostingIsDelivered( );
    }

    @Test
    void weCanSendOnCommand () {
        checker.givenSubscriptionAtBrokerFor( "/" + twinID + "/SET/led" );
        checker.whenSendingOnCommand( "led" );
        checker.thenPostingIsDelivered( );
    }

    @Test
    void weCanSendOffCommand () {
        checker.givenSubscriptionAtBrokerFor( "/" + twinID + "/SET/led" );
        checker.whenSendingOffCommand( "led" );
        checker.thenPostingIsDelivered( );
    }
}
