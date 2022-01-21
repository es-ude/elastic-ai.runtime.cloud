package de.ude.es.source;

import de.ude.es.Checker;
import de.ude.es.TimerMock;
import de.ude.es.comm.Protocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TestStartableDataSource {

    private static class DataSourceChecker extends Checker {

        public TimerMock timer;
        public ControllableDataSource<Double> dataSource;

        public void givenDataSource() {
            timer = new TimerMock();
            Protocol protocol = new Protocol(twin);
            dataSource = new ControllableDataSource<>("/data", timer);
            dataSource.bind(protocol);
        }

        public void givenDataStartPostPublishedBy(String sink) {
            var topic = "/twin1234/START/data";
            whenPostingIsPublishedAtBroker(topic, sink);
        }

        public void givenDigitalTwin() {
            givenDigitalTwin("/twin1234");
        }

        public void whenTimeoutOccurs() {
            timer.fire();
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
        checker.givenDigitalTwin();
        checker.givenDataSource();

        checker.thenSubscriptionIsDoneFor("/START/data");
        checker.thenSubscriptionIsDoneFor("/STOP/data");
    }

    @Test
    void whenStartRequestIsSentThenTemperatureSourceReceivesIt() {

        checker.givenBroker();
        checker.givenDigitalTwin();
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
        checker.givenDigitalTwin();
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
    void whenReceivingStartRequestThenTemperatureSourceSubscribesForHeartbeats() {

        checker.givenBroker();
        checker.givenDigitalTwin("/twin1234");
        checker.givenDataSource();

        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/START/data",
                "/me"
        );
        checker.thenSubscriptionIsDoneFor("/me/HEART");
    }

    @Test
    void whenReceivingStopRequestThenTemperatureSourceUnsubscribesFromHeartbeats() {

        checker.givenBroker();
        checker.givenDigitalTwin("/twin1234");
        checker.givenDataSource();
        checker.givenDataStartPostPublishedBy("/me");

        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/STOP/data",
                "/me"
        );
        checker.thenUnsubscribeIsDoneFor("/me/HEART");
        checker.thenDataSourceHasNoClients();
    }

    @Test
    void whenReceivingStartAfterStopRequestThenTemperatureSourceStartsSendingAgain() {

        checker.givenBroker();
        checker.givenDigitalTwin("/twin1234");
        checker.givenDataSource();
        checker.givenDataStartPostPublishedBy("/me");

        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/STOP/data",
                "/me"
        );
        checker.thenUnsubscribeIsDoneFor("/me/HEART");
        checker.thenDataSourceHasNoClients();
        checker.whenPostingIsPublishedAtBroker(
                "/twin1234/START/data",
                "/m_e"
        );
        checker.thenSubscriptionIsDoneFor("/m_e/HEART");
        checker.thenDataSourceHasClients();
    }

    @Test
    void whenRequesterHeartbeatsTimeOutThenTemperatureSourceRemovesItAsClient() {

        checker.givenBroker();
        checker.givenDigitalTwin("/twin1234");
        checker.givenDataSource();
        checker.givenDataStartPostPublishedBy("/me");
        checker.thenDataSourceHasClients();

        checker.whenTimeoutOccurs();
        checker.thenDataSourceHasNoClients();
        checker.thenUnsubscribeIsDoneFor("/me/HEART");
    }

}