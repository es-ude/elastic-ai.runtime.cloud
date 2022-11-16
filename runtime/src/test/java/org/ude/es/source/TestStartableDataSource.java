package org.ude.es.source;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.Checker;
import org.ude.es.comm.PostingType;

class TestStartableDataSource {

    private static final String DATA_ID = "/data";
    private static final String SOURCE_ID = "/source";
    private static final String CONSUMER_ID = "/consumer";

    private static class DataSourceChecker extends Checker {

        public ControllableDataSource<Double> dataSource;

        public void givenDataSource() {
            dataSource = new ControllableDataSource<>(javaTwin, DATA_ID);
        }

        public void givenDataStartPostPublishedBy(String sink) {
            var topic = SOURCE_ID + PostingType.START.topic(DATA_ID);
            whenPostingIsPublishedAtBroker(topic, sink);
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
    public void SetUp() {
        checker = new DataSourceChecker();
        checker.givenBroker();
        checker.givenJavaTwin(SOURCE_ID);
        checker.givenDataSource();
    }

    @Test
    void whenTemperatureSourceIsBoundItSubscribesForStartAndStop() {
        checker.thenSubscriptionIsDoneFor(PostingType.START.topic(DATA_ID));
        checker.thenSubscriptionIsDoneFor(PostingType.STOP.topic(DATA_ID));
    }

    @Test
    void whenStartRequestIsSentThenTemperatureSourceReceivesIt() {
        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.START.topic(DATA_ID),
            CONSUMER_ID
        );
        checker.thenDataSourceHasClients();
    }

    @Test
    void whenStopRequestIsSentThenTemperatureSourceReceivesIt() {
        checker.givenDataStartPostPublishedBy(CONSUMER_ID);
        checker.thenDataSourceHasClients();
        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.STOP.topic(DATA_ID),
            CONSUMER_ID
        );
        checker.thenDataSourceHasNoClients();
    }

    @Test
    void whenReceivingStartRequestThenTemperatureSourceSubscribesForStatus() {
        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.START.topic(DATA_ID),
            CONSUMER_ID
        );
        checker.thenSubscriptionIsDoneFor(
            CONSUMER_ID + PostingType.STATUS.topic("")
        );
    }

    @Test
    void whenReceivingStopRequestThenTemperatureSourceUnsubscribesFromStatus() {
        checker.givenDataStartPostPublishedBy(CONSUMER_ID);
        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.STOP.topic(DATA_ID),
            CONSUMER_ID
        );
        checker.thenUnsubscribeIsDoneFor(
            CONSUMER_ID + PostingType.STATUS.topic("")
        );
        checker.thenDataSourceHasNoClients();
    }

    @Test
    void whenReceivingStartAfterStopRequestThenTemperatureSourceStartsSendingAgain() {
        checker.givenDataStartPostPublishedBy(CONSUMER_ID);
        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.STOP.topic(DATA_ID),
            CONSUMER_ID
        );
        checker.thenUnsubscribeIsDoneFor(
            CONSUMER_ID + PostingType.STATUS.topic("")
        );
        checker.thenDataSourceHasNoClients();
        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.START.topic(DATA_ID),
            CONSUMER_ID + "1"
        );
        checker.thenSubscriptionIsDoneFor(
            CONSUMER_ID + "1" + PostingType.STATUS.topic("")
        );
        checker.thenDataSourceHasClients();
    }
}
