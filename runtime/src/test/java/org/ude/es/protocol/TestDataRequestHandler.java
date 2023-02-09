package org.ude.es.protocol;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.Checker;

import java.util.concurrent.atomic.AtomicReference;

public class TestDataRequestHandler {

    Checker checker;
    DataRequestHandler dataRequestHandler;

    @BeforeEach
    void beforeEach() {
        checker = new Checker();
        checker.givenBroker();
        checker.givenJavaTwin("test");
        dataRequestHandler = new DataRequestHandler(checker.javaTwin, "data");
    }

    @AfterEach
    void afterEach() {
        checker.whenPostingIsPublishedAtBroker("requester/STATUS", ";0");
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
        checker.whenPostingIsPublishedAtBroker("requester/STATUS", ";0");

        Assertions.assertTrue(received.get());
    }

}
