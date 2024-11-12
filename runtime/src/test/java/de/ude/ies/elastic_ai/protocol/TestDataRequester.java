package de.ude.ies.elastic_ai.protocol;

import de.ude.ies.elastic_ai.Checker;
import de.ude.ies.elastic_ai.communicationEndpoints.RemoteCommunicationEndpoint;
import de.ude.ies.elastic_ai.protocol.requests.DataRequester;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestDataRequester {

    Checker checker;
    DataRequester dataRequester;
    RemoteCommunicationEndpoint stub;

    @BeforeEach
    void beforeEach() {
        checker = new Checker();
        checker.givenBroker();
        checker.givenLocalEndpoint("test");

        stub = new RemoteCommunicationEndpoint("stub");
        stub.bindToCommunicationEndpoint(checker.localEndpoint.getBroker());
        dataRequester = new DataRequester(stub, "data", checker.localEndpoint.getIdentifier());
    }

    @Test
    void dataStartRequestIsPublishedWhenStartedAndDeviceOnline() {
        checker.givenSubscriptionAtBrokerFor("stub/START/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATE:ONLINE;");
        dataRequester.startRequestingData();

        checker.isExpecting(new Posting(checker.DOMAIN + "/stub/START/data", "test"));
        checker.thenPostingIsDelivered();
        checker.thenSubscriptionIsDoneFor("stub/DATA/data");
    }

    @Test
    void dataStopRequestIsPublishedWhenStoppedAndDeviceOnline() {
        checker.givenSubscriptionAtBrokerFor("stub/STOP/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATE:ONLINE;");
        dataRequester.startRequestingData();
        dataRequester.stopRequestingData();

        checker.isExpecting(new Posting(checker.DOMAIN + "/stub/STOP/data", "test"));
        checker.thenPostingIsDelivered();
    }

    @Test
    void dataIsRequestedAgainWhenDeviceComesBackOnline() {
        dataRequester.startRequestingData();

        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATE:ONLINE;");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATE:OFFLINE;");

        checker.givenSubscriptionAtBrokerFor("stub/START/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATE:ONLINE;");
        checker.isExpecting(new Posting(checker.DOMAIN + "/stub/START/data", "test"));
        checker.thenPostingIsDelivered();
    }

    @Test
    void dataIsNotReceivedWhenNotRequested() {
        AtomicReference<String> value = new AtomicReference<>("notSet");
        dataRequester.setDataReceiveFunction(value::set);

        checker.whenPostingIsPublishedAtBroker("stub/DATA/data", "testData");

        Assertions.assertEquals("notSet", value.get());
    }

    @Test
    void dataIsReceivedWhenDataIsRequested() {
        AtomicReference<String> value = new AtomicReference<>("notSet");
        dataRequester.setDataReceiveFunction(value::set);

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
    void multipleStartRequests() {
        checker.givenSubscriptionAtBrokerFor("stub/START/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATE:ONLINE;");
        checker.isExpecting(new Posting(checker.DOMAIN + "/stub/START/data", "test"));
        dataRequester.startRequestingData();
        checker.thenPostingIsDelivered();
        checker.clearPostings();
        dataRequester.startRequestingData();
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void multipleStopRequests() {
        checker.givenSubscriptionAtBrokerFor("stub/STOP/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", "STATE:ONLINE;");

        checker.isExpecting(new Posting(checker.DOMAIN + "/stub/STOP/data", "test"));

        dataRequester.startRequestingData();
        dataRequester.stopRequestingData();
        checker.thenPostingIsDelivered();
        checker.clearPostings();
        dataRequester.startRequestingData();
        checker.thenPostingIsNotDelivered();
    }
}
