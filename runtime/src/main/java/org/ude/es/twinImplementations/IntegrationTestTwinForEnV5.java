package org.ude.es.twinImplementations;

import static java.lang.Thread.sleep;

import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

public class IntegrationTestTwinForEnV5 extends JavaTwin {

    private final TwinStub enV5;

    public IntegrationTestTwinForEnV5(String identifier) {
        super(identifier);
        enV5 = new TwinStub("enV5");
    }

    @Override
    protected void executeOnBind() {
        enV5.bindToCommunicationEndpoint(endpoint);
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
