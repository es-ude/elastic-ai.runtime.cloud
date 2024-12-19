package de.ude.ies.elastic_ai.source;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.ude.ies.elastic_ai.Checker;
import de.ude.ies.elastic_ai.protocol.PostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestAlternativeStartAbleDataSource {

    private static final String DATA_ID = "data";
    private static final String SOURCE_ID = "source";
    private static final String CONSUMER_ID = "consumer";

    private static class DataSourceChecker extends Checker {

        public AlternativeStartAbleDataSource<?> dataSource;

        public void givenDataSource() {
            dataSource = new AlternativeStartAbleDataSource<>(DATA_ID);
            dataSource.bind(localEndpoint);
        }

        public void givenDataStartPostPublishedBy(String sink) {
            var topic = SOURCE_ID + PostingType.START.topic(DATA_ID);
            whenPostingIsPublishedAtBroker(topic, sink);
        }

        public void givenJavaTwin() {
            givenLocalEndpoint(SOURCE_ID);
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

        checker.thenSubscriptionIsDoneFor(PostingType.START.topic(DATA_ID));
        checker.thenSubscriptionIsDoneFor(PostingType.STOP.topic(DATA_ID));
    }

    @Test
    void whenStartRequestIsSentThenTemperatureSourceReceivesIt() {
        checker.givenBroker();
        checker.givenJavaTwin();
        checker.givenDataSource();

        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.START.topic(DATA_ID),
            CONSUMER_ID
        );

        checker.thenDataSourceHasClients();
    }

    @Test
    void whenStopRequestIsSentThenTemperatureSourceReceivesIt() {
        checker.givenBroker();
        checker.givenJavaTwin();
        checker.givenDataSource();
        checker.givenDataStartPostPublishedBy(CONSUMER_ID);
        checker.thenDataSourceHasClients();

        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.STOP.topic(DATA_ID),
            CONSUMER_ID
        );

        checker.thenDataSourceHasNoClients();
    }

    @Test
    void whenReceivingStartRequestThenTemperatureSourceSubscribesForLost() {
        checker.givenBroker();
        checker.givenLocalEndpoint(SOURCE_ID);
        checker.givenDataSource();

        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.START.topic(DATA_ID),
            CONSUMER_ID
        );
        checker.thenSubscriptionIsDoneFor(CONSUMER_ID + PostingType.STATUS.topic(""));
    }

    @Test
    void whenReceivingStopRequestThenTemperatureSourceUnsubscribesFromLost() {
        checker.givenBroker();
        checker.givenLocalEndpoint(SOURCE_ID);
        checker.givenDataSource();
        checker.givenDataStartPostPublishedBy(CONSUMER_ID);

        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.STOP.topic(DATA_ID),
            CONSUMER_ID
        );
        checker.thenUnsubscribeIsDoneFor(CONSUMER_ID + PostingType.STATUS.topic(""));
        checker.thenDataSourceHasNoClients();
    }

    @Test
    void whenReceivingStopRequestThenTemperatureSourceUnsubscribesForCorrectLost() {
        checker.givenBroker();
        checker.givenLocalEndpoint(SOURCE_ID);
        checker.givenDataSource();
        checker.givenDataStartPostPublishedBy(CONSUMER_ID + "1");
        checker.givenDataStartPostPublishedBy(CONSUMER_ID + "2");

        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.STOP.topic(DATA_ID),
            CONSUMER_ID + "1"
        );
        checker.thenUnsubscribeIsDoneFor(CONSUMER_ID + "1" + PostingType.STATUS.topic(""));
    }

    @Test
    void whenReceivingStartAfterStopRequestThenTemperatureSourceStartsSendingAgain() {
        checker.givenBroker();
        checker.givenLocalEndpoint(SOURCE_ID);
        checker.givenDataSource();
        checker.givenDataStartPostPublishedBy(CONSUMER_ID);

        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.STOP.topic(DATA_ID),
            CONSUMER_ID
        );
        checker.whenPostingIsPublishedAtBroker(
            SOURCE_ID + PostingType.START.topic(DATA_ID),
            CONSUMER_ID
        );
        checker.thenSubscriptionIsDoneFor(CONSUMER_ID + PostingType.STATUS.topic(""));
        checker.thenDataSourceHasClients();
    }
}
