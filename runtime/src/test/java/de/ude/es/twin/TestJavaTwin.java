package de.ude.es.twin;


import de.ude.es.Checker;
import de.ude.es.comm.Posting;
import de.ude.es.comm.PostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class TestJavaTwin {

    private static final String twinID = "test";

    private static class JavaTwinChecker extends Checker {
        public JavaTwin device;

        public void givenDevice () {
            device = new JavaTwin( twinID );
            device.bind( broker );
        }

        public void whenPublishingData ( String dataId, String value ) {
            String topic = device.ID( ) + PostingType.DATA.topic( dataId );
            expected = new Posting( topic, value );
            device.publishData( dataId, value );
        }

        public void whenSubscribingForDataStart ( String dataId ) {
            device.subscribeForDataStartRequest( dataId, subscriber );
        }

        public void whenUnsubscribingFromDataStart ( String dataId ) {
            device.unsubscribeFromDataStartRequest( dataId, subscriber );
        }

        public void whenSubscribingForDataStop ( String dataId ) {
            device.subscribeForDataStopRequest( dataId, subscriber );
        }

        public void whenUnsubscribingFromDataStop ( String dataId ) {
            device.unsubscribeFromDataStopRequest( dataId, subscriber );
        }

        public void whenSubscribingForCommand ( String dataId ) {
            device.subscribeForCommand( dataId, subscriber );
        }

        public void whenUnsubscribingFromCommand ( String dataId ) {
            device.unsubscribeFromCommand( dataId, subscriber );
        }

    }

    private JavaTwinChecker checker;

    @BeforeEach
    public void setUp () {
        checker = new JavaTwinChecker( );
        checker.givenBroker( );
        checker.givenDevice( );
    }

    @Test
    void weCanPublishData () {
        checker.givenSubscriptionAtBrokerFor(
                "/" + twinID + "/DATA/temperature" );
        checker.whenPublishingData( "temperature", "13.5" );
        checker.thenPostingIsDelivered( );
    }

    @Test
    void weCanSubscribeForDataStartRequest () {
        checker.whenSubscribingForDataStart( "data" );
        checker.whenPostingIsPublishedAtBroker( "/" + twinID + "/START/data",
                                                twinID );
        checker.thenPostingIsDelivered( );
    }

    @Test
    void weCanUnsubscribeFromDataStartRequest () {
        checker.whenSubscribingForDataStart( "data" );
        checker.whenUnsubscribingFromDataStart( "data" );
        checker.whenPostingIsPublishedAtBroker( "/" + twinID + "/START/data",
                                                twinID );
        checker.thenPostingIsNotDelivered( );
    }

    @Test
    void weCanSubscribeForDataStopRequest () {
        checker.whenSubscribingForDataStop( "data" );
        checker.whenPostingIsPublishedAtBroker( "/" + twinID + "/STOP/data",
                                                twinID );
        checker.thenPostingIsDelivered( );
    }

    @Test
    void weCanUnsubscribeFromDataStopRequest () {
        checker.whenSubscribingForDataStop( "data" );
        checker.whenUnsubscribingFromDataStop( "data" );
        checker.whenPostingIsPublishedAtBroker( "/" + twinID + "/STOP/data",
                                                twinID );
        checker.thenPostingIsNotDelivered( );
    }

    @Test
    void weCanSubscribeForCommand () {
        checker.whenSubscribingForCommand( "data" );
        checker.whenPostingIsPublishedAtBroker( "/" + twinID + "/SET/data",
                                                twinID );
        checker.thenPostingIsDelivered( );
    }

    @Test
    void weCanUnsubscribeFromCommand () {
        checker.whenSubscribingForCommand( "data" );
        checker.whenUnsubscribingFromCommand( "data" );
        checker.whenPostingIsPublishedAtBroker( "/" + twinID + "/SET/data",
                                                twinID );
        checker.thenPostingIsNotDelivered( );
    }

}
