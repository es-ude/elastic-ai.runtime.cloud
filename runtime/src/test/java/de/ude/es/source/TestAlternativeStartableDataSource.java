package de.ude.es.source;

import de.ude.es.Checker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TestAlternativeStartableDataSource {

    private static class DataSourceChecker extends Checker {

        public AlternativeStartableDataSource<?> dataSource;

        public void givenDataSource() {
            dataSource = new AlternativeStartableDataSource<>("data");
            dataSource.bind(javaTwin);
        }

        public void givenDataStartPostPublishedBy(String sink) {
            var topic = "/twin1234/START/data";
            whenPostingIsPublishedAtBroker(topic, sink);
        }

        public void givenJavaTwin() {
            givenJavaTwin("/twin1234");
        }

        public void thenDataSourceHasClients() {
            assertTrue(dataSource.hasClients());
        }

        public void thenDataSourceHasNoClients() {
            assertFalse(dataSource.hasClients());
        }

    }


    private DataSourceChecker checker;


    @BeforeEach
    void init() {
        checker = new DataSourceChecker();
    }

    @Test
    void whenTemperatureSourceIsBoundItSubscribesForStartAndStop() {

        checker.givenBroker();
        checker.givenJavaTwin();
        checker.givenDataSource();

        checker.thenSubscriptionIsDoneFor("/START/data");
        checker.thenSubscriptionIsDoneFor("/STOP/data");
    }

    @Test
    void whenStartRequestIsSentThenTemperatureSourceReceivesIt() {

        checker.givenBroker();
        checker.givenJavaTwin();
        checker.givenDataSource();

        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/START/data",
                "/me"
        );

        checker.thenDataSourceHasClients();
    }

    @Test
    void whenStopRequestIsSentThenTemperatureSourceReceivesIt() {

        checker.givenBroker();
        checker.givenJavaTwin();
        checker.givenDataSource();
        checker.givenDataStartPostPublishedBy("/me");
        checker.thenDataSourceHasClients();

        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/STOP/data",
                "/me"
        );

        checker.thenDataSourceHasNoClients();
    }

    @Test
    void whenReceivingStartRequestThenTemperatureSourceSubscribesForLost() {

        checker.givenBroker();
        checker.givenJavaTwin("/twin1234");
        checker.givenDataSource();

        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/START/data",
                "/me"
        );
        checker.thenSubscriptionIsDoneFor("/me/LOST");
    }

    @Test
    void whenReceivingStopRequestThenTemperatureSourceUnsubscribesFromLost() {
        checker.givenBroker();
        checker.givenJavaTwin("/twin1234");
        checker.givenDataSource();
        checker.givenDataStartPostPublishedBy("/me");

        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/STOP/data",
                "/me"
        );
        checker.thenUnsubscribeIsDoneFor("/me/LOST");
        checker.thenDataSourceHasNoClients();
    }

    @Test
    void whenReceivingStopRequestThenTemperatureSourceUnsubscribesForCorrectLost() {

        checker.givenBroker();
        checker.givenJavaTwin("/twin1234");
        checker.givenDataSource();
        checker.givenDataStartPostPublishedBy("/me");
        checker.givenDataStartPostPublishedBy("/m_e");

        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/STOP/data",
                "/me"
        );
        checker.thenUnsubscribeIsDoneFor("/me/LOST");
    }

    @Test
    void whenReceivingStartAfterStopRequestThenTemperatureSourceStartsSendingAgain() {
        checker.givenBroker();
        checker.givenJavaTwin("/twin1234");
        checker.givenDataSource();
        checker.givenDataStartPostPublishedBy("/me");

        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/STOP/data",
                "/me"
        );
        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/START/data",
                "/m_e"
        );
        checker.thenSubscriptionIsDoneFor("/m_e/LOST");
        checker.thenDataSourceHasClients();
    }

}