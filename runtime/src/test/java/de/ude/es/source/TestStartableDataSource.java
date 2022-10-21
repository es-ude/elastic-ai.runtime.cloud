package de.ude.es.source;

<<<<<<< HEAD
=======
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
>>>>>>> main

import de.ude.es.Checker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

<<<<<<< HEAD
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


=======
>>>>>>> main
class TestStartableDataSource {

    private static class DataSourceChecker extends Checker {

        public ControllableDataSource <Double> dataSource;

        public void givenDataSource () {
            dataSource = new ControllableDataSource <>( "data" );
            dataSource.bind( javaTwin );
        }

        public void givenDataStartPostPublishedBy ( String sink ) {
            var topic = "/twin1234/START/data";
            whenPostingIsPublishedAtBroker( topic, sink );
        }

        public void givenJavaTwin () {
            givenJavaTwin( "/twin1234" );
        }

        public void thenDataSourceHasClients () {
            assertTrue( dataSource.hasClients( ) );
        }

        public void thenDataSourceHasNoClients () {
            assertFalse( dataSource.hasClients( ) );
        }
    }

    private DataSourceChecker checker;

    @BeforeEach
    public void SetUp () {
        checker = new DataSourceChecker( );
        checker.givenBroker( );
        checker.givenJavaTwin( "/twin1234" );
        checker.givenDataSource( );
    }

    @Test
    void whenTemperatureSourceIsBoundItSubscribesForStartAndStop () {
        checker.thenSubscriptionIsDoneFor( "/START/data" );
        checker.thenSubscriptionIsDoneFor( "/STOP/data" );
    }

    @Test
<<<<<<< HEAD
    void whenStartRequestIsSentThenTemperatureSourceReceivesIt () {
        checker.whenPostingIsPublishedAtBroker( "/twin1234/START/data", "/me" );
=======
    void whenStartRequestIsSentThenTemperatureSourceReceivesIt() {
        checker.whenPostingIsPublishedAtBroker("/twin1234/START/data", "/me");
>>>>>>> main

        checker.thenDataSourceHasClients( );
    }

    @Test
    void whenStopRequestIsSentThenTemperatureSourceReceivesIt () {
        checker.givenDataStartPostPublishedBy( "/me" );
        checker.thenDataSourceHasClients( );

<<<<<<< HEAD
        checker.whenPostingIsPublishedAtBroker( "/twin1234/STOP/data", "/me" );
=======
        checker.whenPostingIsPublishedAtBroker("/twin1234/STOP/data", "/me");
>>>>>>> main

        checker.thenDataSourceHasNoClients( );
    }

    @Test
<<<<<<< HEAD
    void whenReceivingStartRequestThenTemperatureSourceSubscribesForStatus () {
        checker.whenPostingIsPublishedAtBroker( "/twin1234/START/data", "/me" );
        checker.thenSubscriptionIsDoneFor( "/me/STATUS" );
    }

    @Test
    void whenReceivingStopRequestThenTemperatureSourceUnsubscribesFromStatus () {
        checker.givenDataStartPostPublishedBy( "/me" );
        checker.whenPostingIsPublishedAtBroker( "/twin1234/STOP/data", "/me" );
        checker.thenUnsubscribeIsDoneFor( "/me/STATUS" );
        checker.thenDataSourceHasNoClients( );
    }

    @Test
    void whenReceivingStartAfterStopRequestThenTemperatureSourceStartsSendingAgain () {
        checker.givenDataStartPostPublishedBy( "/me" );
        checker.whenPostingIsPublishedAtBroker( "/twin1234/STOP/data", "/me" );
        checker.thenUnsubscribeIsDoneFor( "/me/STATUS" );
        checker.thenDataSourceHasNoClients( );
        checker.whenPostingIsPublishedAtBroker( "/twin1234/START/data",
                                                "/m_e" );
        checker.thenSubscriptionIsDoneFor( "/m_e/STATUS" );
        checker.thenDataSourceHasClients( );
=======
    void whenReceivingStartRequestThenTemperatureSourceSubscribesForHeartbeats() {
        checker.whenPostingIsPublishedAtBroker("/twin1234/START/data", "/me");
        checker.thenSubscriptionIsDoneFor("/me/HEART");
    }

    @Test
    void whenReceivingStopRequestThenTemperatureSourceUnsubscribesFromHeartbeats() {
        checker.givenDataStartPostPublishedBy("/me");
        checker.whenPostingIsPublishedAtBroker("/twin1234/STOP/data", "/me");
        checker.thenUnsubscribeIsDoneFor("/me/HEART");
        checker.thenDataSourceHasNoClients();
    }

    @Test
    void whenReceivingStartAfterStopRequestThenTemperatureSourceStartsSendingAgain() {
        checker.givenDataStartPostPublishedBy("/me");
        checker.whenPostingIsPublishedAtBroker("/twin1234/STOP/data", "/me");
        checker.thenUnsubscribeIsDoneFor("/me/HEART");
        checker.thenDataSourceHasNoClients();
        checker.whenPostingIsPublishedAtBroker("/twin1234/START/data", "/m_e");
        checker.thenSubscriptionIsDoneFor("/m_e/HEART");
        checker.thenDataSourceHasClients();
    }

    @Test
    void whenRequesterHeartbeatsTimeOutThenTemperatureSourceRemovesItAsClient() {
        checker.givenDataStartPostPublishedBy("/me");
        checker.thenDataSourceHasClients();

        checker.whenTimeoutOccurs();
        checker.thenDataSourceHasNoClients();
        checker.thenUnsubscribeIsDoneFor("/me/HEART");
>>>>>>> main
    }
}
