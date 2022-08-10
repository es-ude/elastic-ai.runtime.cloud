package de.ude.es.twin;

import de.ude.es.comm.Posting;
import de.ude.es.comm.PostingType;
import de.ude.es.comm.Subscriber;

public class JavaTwin extends Twin {

    public JavaTwin(String identifier) {
        super(identifier);
    }

    public void publishData(String dataId, String value) {
        this.publish(Posting.createData(dataId, value));
    }

    public void publishHeartbeat(String who) {
        this.publish(Posting.createHeartbeat(who));
    }

    public void subscribeForDataStartRequest(String dataId, Subscriber subscriber) {
        this.subscribe(
                PostingType.START.topic(dataId),
                subscriber);
    }

    public void unsubscribeFromDataStartRequest(String dataId, Subscriber subscriber) {
        this.unsubscribe(
                PostingType.START.topic(dataId),
                subscriber);
    }

    public void subscribeForDataStopRequest(String dataId, Subscriber subscriber) {
        this.subscribe(
                PostingType.STOP.topic(dataId),
                subscriber);
    }

    public void unsubscribeFromDataStopRequest(String dataId, Subscriber subscriber) {
        this.unsubscribe(
                PostingType.STOP.topic(dataId),
                subscriber);
    }

    public void subscribeForCommand(String dataId, Subscriber subscriber) {
        this.subscribe(
                PostingType.SET.topic(dataId),
                subscriber);
    }

    public void unsubscribeFromCommand(String dataId, Subscriber subscriber) {
        this.unsubscribe(
                PostingType.SET.topic(dataId),
                subscriber);
    }

}
