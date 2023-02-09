package org.ude.es.protocol;

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
        dataRequester = new DataRequester(stub, "data", checker.javaTwin.getIdentifier());
    }

    @Test
    void dataStartRequestIsPublishedWhenStartedAndDeviceOnline() {
        checker.givenSubscriptionAtBrokerFor("stub/START/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", ";1");
        dataRequester.startRequestingData();

        checker.expected = new Posting(checker.DOMAIN + "/stub/START/data", "test");
        checker.thenPostingIsDelivered();
        checker.thenSubscriptionIsDoneFor("stub/DATA/data");
    }

    @Test
    void dataStopRequestIsPublishedWhenStartedAndDeviceOnline() {
        checker.givenSubscriptionAtBrokerFor("stub/STOP/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", ";1");
        dataRequester.startRequestingData();
        dataRequester.stopRequestingData();

        checker.expected = new Posting(checker.DOMAIN + "/stub/STOP/data", "test");
        checker.thenPostingIsDelivered();
    }

    @Test
    void dataIsRequestedAgainWhenDeviceComesBackOnline() {
        dataRequester.startRequestingData();

        checker.whenPostingIsPublishedAtBroker("stub/STATUS", ";1");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", ";0");

        checker.givenSubscriptionAtBrokerFor("stub/START/data");
        checker.whenPostingIsPublishedAtBroker("stub/STATUS", ";1");
        checker.expected = new Posting(checker.DOMAIN + "/stub/START/data", "test");
        checker.thenPostingIsDelivered();
    }

}
