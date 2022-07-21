package de.ude.es.twin;

import de.ude.es.comm.Subscriber;

public class StubTwin extends Twin {

    public StubTwin(String identifier) {
        super(identifier);
    }

    public void subscribe(String topic, Subscriber subscriber) {
        endpoint.subscribe(identifier + topic, subscriber);
    }

    public void unsubscribe(String topic, Subscriber subscriber) {
        endpoint.unsubscribe(identifier + topic, subscriber);
    }

}
