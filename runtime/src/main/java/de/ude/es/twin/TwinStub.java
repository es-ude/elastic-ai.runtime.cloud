package de.ude.es.twin;

import de.ude.es.comm.Posting;
import de.ude.es.comm.PostingType;
import de.ude.es.comm.Subscriber;

public class TwinStub extends Twin {

    public TwinStub(String identifier) {
        super(identifier);
    }

    public void subscribeForData(String dataId, Subscriber subscriber) {
        this.subscribe(
                PostingType.DATA.topic(dataId),
                subscriber);
    }

    public void unsubscribeFromData(String dataId, Subscriber subscriber) {
        this.unsubscribe(
                PostingType.DATA.topic(dataId),
                subscriber);
    }

    public void subscribeForHeartbeat(Subscriber subscriber) {
        var topic = PostingType.HEARTBEAT.topic("");
        this.subscribe(topic, subscriber);
    }

    public void unsubscribeFromHeartbeat(Subscriber subscriber) {
        var topic = PostingType.HEARTBEAT.topic("");
        this.unsubscribe(topic, subscriber);
    }

    public void subscribeForLost(Subscriber subscriber) {
        this.subscribe(
                PostingType.LOST.topic(""),
                subscriber);
    }

    public void unsubscribeFromLost(Subscriber subscriber) {
        this.unsubscribe(
                PostingType.LOST.topic(""),
                subscriber);
    }

    public void publishDataStartRequest(String dataId, String receiver) {
        Posting post = Posting.createStartSending(dataId, receiver);
        this.publish(post);
    }

    public void publishDataStopRequest(String dataId, String receiver) {
        Posting post = Posting.createStopSending(dataId, receiver);
        this.publish(post);
    }

    public void publishCommand(String service, String cmd) {
        Posting post = Posting.createCommand(service, cmd);
        this.publish(post);
    }

    public void publishOnCommand(String service) {
        Posting post = Posting.createTurnOn(service);
        this.publish(post);
    }

    public void publishOffCommand(String service) {
        Posting post = Posting.createTurnOff(service);
        this.publish(post);
    }

}
