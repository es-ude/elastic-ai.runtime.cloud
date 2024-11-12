package de.ude.ies.elastic_ai.protocol;

import de.ude.ies.elastic_ai.Checker;
import de.ude.ies.elastic_ai.protocol.requests.DataRequestHandler;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestDataRequestHandler {

    Checker checker;
    DataRequestHandler dataRequestHandler;

    @BeforeEach
    void beforeEach() {
        checker = new Checker();
        checker.givenBroker();
        checker.givenLocalEndpoint("test");
        dataRequestHandler = new DataRequestHandler(checker.localEndpoint, "data");
    }

    @AfterEach
    void afterEach() {
        checker.whenPostingIsPublishedAtBroker("requester/STATUS", "STATUS:OFFLINE;");
    }

    @Test
    void subscribeForStatusWhenDataIsRequested() {
        checker.whenPostingIsPublishedAtBroker("test/START/data", "requester");
        checker.thenSubscriptionIsDoneFor("requester/STATUS");
    }

    @Test
    void executeDataStartRequestHandlersWhenDataIsRequested() {
        AtomicReference<Boolean> received = new AtomicReference<>(false);

        dataRequestHandler.addWhenStartRequestingData(() -> received.set(true));
        checker.whenPostingIsPublishedAtBroker("test/START/data", "requester");

        Assertions.assertTrue(received.get());
    }

    @Test
    void subscribeForDataStartAndStop() {
        checker.thenSubscriptionIsDoneFor("test/START/data");
        checker.thenSubscriptionIsDoneFor("test/STOP/data");
    }

    @Test
    void unsubscribeForStatusWhenDataIsNoLongerRequested() {
        checker.whenPostingIsPublishedAtBroker("test/START/data", "requester");
        checker.whenPostingIsPublishedAtBroker("test/STOP/data", "requester");

        checker.thenUnsubscribeIsDoneFor("requester/STATUS");
    }

    @Test
    void executeDataStopRequestHandlersWhenDataIsNoLongerRequested() {
        AtomicReference<Boolean> received = new AtomicReference<>(false);

        dataRequestHandler.addWhenStopRequestingData(() -> received.set(true));
        checker.whenPostingIsPublishedAtBroker("test/START/data", "requester");
        checker.whenPostingIsPublishedAtBroker("test/STOP/data", "requester");

        Assertions.assertTrue(received.get());
    }

    @Test
    void stopRequestingDataWhenRequesterGetsOffline() {
        AtomicReference<Boolean> received = new AtomicReference<>(false);

        dataRequestHandler.addWhenStopRequestingData(() -> received.set(true));
        checker.whenPostingIsPublishedAtBroker("test/START/data", "requester");
        checker.whenPostingIsPublishedAtBroker("requester/STATUS", "STATUS:OFFLINE;");

        Assertions.assertTrue(received.get());
    }

    @Test
    void publishedNewDataToSubscribers() {
        checker.givenSubscriptionAtBrokerFor("test/DATA/data");

        dataRequestHandler.newDataToPublish("testData");

        checker.isExpecting(new Posting(checker.DOMAIN + "/test/DATA/data", "testData"));
        checker.thenPostingIsDelivered();
    }

    @Test
    void duplicatedSubscriptionsAreIgnored() {
        checker.whenPostingIsPublishedAtBroker("test/START/data", "requester");
        checker.whenPostingIsPublishedAtBroker("test/START/data", "requester");

        checker.whenPostingIsPublishedAtBroker("test/STOP/data", "requester");

        checker.thenUnsubscribeIsDoneFor("requester/STATUS");
    }

    @Test
    void multipleValuesAreRequestedBySameRequester() {
        DataRequestHandler dataRequestHandler1 = new DataRequestHandler(
            checker.localEndpoint,
            "data1"
        );
        AtomicReference<Boolean> received = new AtomicReference<>(false);
        dataRequestHandler1.addWhenStartRequestingData(() -> received.set(true));

        checker.whenPostingIsPublishedAtBroker("test/START/data", "requester");
        checker.whenPostingIsPublishedAtBroker("test/START/data1", "requester");
        checker.whenPostingIsPublishedAtBroker("test/STOP/data", "requester");

        checker.whenPostingIsPublishedAtBroker("test/DATA/data", "data");
        Assertions.assertEquals(true, received.get());

        checker.whenPostingIsPublishedAtBroker("test/STOP/data1", "requester");
        checker.thenUnsubscribeIsDoneFor("requester/STATUS");
    }

    @Test
    void valueRequestedBuMultipleRequesters() {
        checker.whenPostingIsPublishedAtBroker("test/START/data", "requester");
        checker.whenPostingIsPublishedAtBroker("test/START/data", "requester1");

        checker.whenPostingIsPublishedAtBroker("test/STOP/data", "requester");
        checker.thenUnsubscribeIsDoneFor("requester/STATUS");

        checker.whenPostingIsPublishedAtBroker("test/STOP/data", "requester1");
        checker.thenUnsubscribeIsDoneFor("requester1/STATUS");
    }

    @Test
    void test() {
        checker.whenPostingIsPublishedAtBroker("test/STOP/data", "requester");
        checker.thenPostingIsNotDelivered();
    }
}
