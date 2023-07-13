package org.ude.es.protocol;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.Checker;
import org.ude.es.comm.Posting;
import org.ude.es.twinBase.TwinStub;

public class TestDataRequester {

    Checker checker;
    DataRequester dataRequester;
    TwinStub stub;

    @BeforeEach
    void beforeEach() {
        checker = new Checker();
        checker.givenBroker();
        checker.givenJavaTwin("test");

        stub = new TwinStub("stub");
        stub.bindToCommunicationEndpoint(checker.javaTwin.getEndpoint());
        dataRequester =
            new DataRequester(stub, "data", checker.javaTwin.getIdentifier());
    }

    @Test
    void dataStartRequestIsPublishedWhenStartedAndDeviceOnline() {
        checker.givenSubscriptionAtBrokerFor("stub/START/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATUS:ONLINE;");
        dataRequester.startRequestingData();

        checker.isExpecting(
            new Posting(checker.DOMAIN + "/stub/START/data", "test")
        );
        checker.thenPostingIsDelivered();
        checker.thenSubscriptionIsDoneFor("stub/DATA/data");
    }

    @Test
    void dataStopRequestIsPublishedWhenStoppedAndDeviceOnline() {
        checker.givenSubscriptionAtBrokerFor("stub/STOP/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATUS:ONLINE;");
        dataRequester.startRequestingData();
        dataRequester.stopRequestingData();

        checker.isExpecting(
            new Posting(checker.DOMAIN + "/stub/STOP/data", "test")
        );
        checker.thenPostingIsDelivered();
    }

    @Test
    void dataIsRequestedAgainWhenDeviceComesBackOnline() {
        dataRequester.startRequestingData();

        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATUS:ONLINE;");
        checker.whenPostingIsPublishedAtBroker(
            "stub/STATUS",
            "STATUS:OFFLINE;"
        );

        checker.givenSubscriptionAtBrokerFor("stub/START/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATUS:ONLINE;");
        checker.isExpecting(
            new Posting(checker.DOMAIN + "/stub/START/data", "test")
        );
        checker.thenPostingIsDelivered();
    }

    @Test
    void dataIsNotReceivedWhenNotRequested() {
        AtomicReference<String> value = new AtomicReference<>("notSet");
        dataRequester.addWhenNewDataReceived(value::set);

        checker.whenPostingIsPublishedAtBroker("stub/DATA/data", "testData");

        Assertions.assertEquals("notSet", value.get());
    }

    @Test
    void dataIsReceivedWhenDataIsRequested() {
        AtomicReference<String> value = new AtomicReference<>("notSet");
        dataRequester.addWhenNewDataReceived(value::set);

        dataRequester.startRequestingData();
        checker.whenPostingIsPublishedAtBroker("stub/DATA/data", "testData");

        Assertions.assertEquals("testData", value.get());
    }

    @Test
    void postingIsNotDeliveredWhenRequestArePaused() {
        checker.givenSubscriptionAtBrokerFor("stub/STOP/data");
        dataRequester.stopRequestingData();
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void startIsDeliveredAfterRequestsAreResumed() {
        checker.givenSubscriptionAtBrokerFor("stub/START/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATUS:ONLINE;");
        dataRequester.pauseDataRequests();
        dataRequester.startRequestingData();

        checker.isExpecting(
            new Posting(checker.DOMAIN + "/stub/START/data", "test")
        );
        checker.thenPostingIsNotDelivered();

        dataRequester.resumeDataRequests();
        checker.thenPostingIsDelivered();
    }

    @Test
    void stopIsDeliveredAfterRequestsAreResumed() {
        checker.givenSubscriptionAtBrokerFor("stub/STOP/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATUS:ONLINE;");
        dataRequester.startRequestingData();
        dataRequester.pauseDataRequests();
        dataRequester.stopRequestingData();

        checker.isExpecting(
            new Posting(checker.DOMAIN + "/stub/STOP/data", "test")
        );
        checker.thenPostingIsNotDelivered();

        dataRequester.resumeDataRequests();
        checker.thenPostingIsDelivered();
    }

    @Test
    void multipleStartRequests() {
        checker.givenSubscriptionAtBrokerFor("stub/START/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATUS:ONLINE;");
        checker.isExpecting(
            new Posting(checker.DOMAIN + "/stub/START/data", "test")
        );
        dataRequester.startRequestingData();
        checker.thenPostingIsDelivered();
        checker.clearPostings();
        dataRequester.startRequestingData();
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void multipleStopRequests() {
        checker.givenSubscriptionAtBrokerFor("stub/STOP/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATUS:ONLINE;");

        checker.isExpecting(
            new Posting(checker.DOMAIN + "/stub/STOP/data", "test")
        );

        dataRequester.startRequestingData();
        dataRequester.stopRequestingData();
        checker.thenPostingIsDelivered();
        checker.clearPostings();
        dataRequester.startRequestingData();
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void test() {
        checker.givenSubscriptionAtBrokerFor("stub/START/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATUS:ONLINE;");

        checker.isExpecting(
            new Posting(checker.DOMAIN + "/stub/START/data", "test")
        );

        dataRequester.startRequestingData();
        dataRequester.pauseDataRequests();
        dataRequester.resumeDataRequests();
        checker.thenPostingIsDelivered();
        checker.clearPostings();
        dataRequester.resumeDataRequests();
        checker.thenPostingIsNotDelivered();
    }
}
