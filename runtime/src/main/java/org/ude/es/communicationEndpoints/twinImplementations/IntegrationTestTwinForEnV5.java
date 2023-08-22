package org.ude.es.communicationEndpoints.twinImplementations;

import static java.lang.Thread.sleep;

import org.ude.es.communicationEndpoints.LocalCommunicationEndpoint;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;

public class IntegrationTestTwinForEnV5 extends LocalCommunicationEndpoint {

    private final RemoteCommunicationEndpoint enV5;

    public IntegrationTestTwinForEnV5(String identifier) {
        super(identifier);
        enV5 = new RemoteCommunicationEndpoint("enV5");
    }

    @Override
    protected void executeOnBind() {
        enV5.bindToCommunicationEndpoint(brokerStub);
    }

    public void startSubscribing(String topic) {
        enV5.subscribeForData(
            topic,
            posting -> System.out.println(posting.data())
        );
    }

    public void startPublishing(int sleep) throws InterruptedException {
        int i = 0;
        while (true) {
            this.publishData("testSub", "testData" + i);
            i++;
            sleep(sleep);
        }
    }
}
