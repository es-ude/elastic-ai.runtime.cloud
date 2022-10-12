package de.ude.es.exampleTwins;


import de.ude.es.Checker;
import de.ude.es.comm.Posting;
import de.ude.es.comm.PostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class TestTwinWithStatus {

    private static class TwinWithStatusChecker extends Checker {
        public TwinWithStatus twinWithStatus;

        public void givenTwinWithStatus ( String id ) {
            twinWithStatus = new TwinWithStatus( id );
            twinWithStatus.bind( broker );
        }

        public void whenTwinIsConnected () {
            twinWithStatus.publishStatus( "ID", true );
        }

        public void thenOnlinePostingIsReceived () {
            subscriber.checkPostingDelivered( new Posting(
                    this.twinWithStatus.ID( ) + PostingType.STATUS.topic( "" ),
                    "ID;1" ) );
        }

        public void whenTwinIsDisconnected () {
            twinWithStatus.publishStatus( "ID", false );
        }

        public void thenOfflinePostingIsReceived () {
            subscriber.checkPostingDelivered( new Posting(
                    this.twinWithStatus.ID( ) + PostingType.STATUS.topic( "" ),
                    "ID;0" ) );
        }
    }

    private TwinWithStatusChecker checker;

    @BeforeEach
    void init () {
        checker = new TwinWithStatusChecker( );
    }

    @Test
    void weReceivedOnlineStatus () {
        checker.givenBroker( );
        checker.givenTwinWithStatus( "/test123" );
        checker.givenSubscriptionAtBrokerFor( "/test123/STATUS" );

        checker.whenTwinIsConnected( );
        checker.thenOnlinePostingIsReceived( );
    }

    @Test
    void weReceivedOfflineStatus () {
        checker.givenBroker( );
        checker.givenTwinWithStatus( "/test123" );
        checker.givenSubscriptionAtBrokerFor( "/test123/STATUS" );

        checker.whenTwinIsConnected( );
        checker.thenOnlinePostingIsReceived( );
        checker.whenTwinIsDisconnected( );
        checker.thenOfflinePostingIsReceived( );
    }

}
