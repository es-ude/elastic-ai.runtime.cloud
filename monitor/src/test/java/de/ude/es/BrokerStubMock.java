package de.ude.es;

import org.ude.es.protocol.BrokerStub;
import org.ude.es.protocol.Posting;
import org.ude.es.protocol.Subscriber;

public abstract class BrokerStubMock implements BrokerStub {

    @Override
    public void connect(String clientId, String lwtMessage) {}

    @Override
    public void publish(Posting posting, boolean retain) {}

    @Override
    public void subscribe(String topic, Subscriber subscriber) {}

    @Override
    public void unsubscribe(String topic) {}

    @Override
    public void subscribeRaw(String topic, Subscriber subscriber) {}

    @Override
    public void unsubscribeRaw(String topic) {}

    @Override
    public String getClientIdentifier() {
        return null;
    }

    @Override
    public String getDomain() {
        return null;
    }
}
