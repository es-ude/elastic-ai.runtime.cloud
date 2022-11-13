package org.ude.es.twinBase;

import org.ude.es.comm.Posting;
import org.ude.es.comm.PostingType;
import org.ude.es.comm.Subscriber;

public class TwinStub extends Twin {

    public TwinStub(String identifier) {
        super(identifier);
    }

    public void subscribeForData(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.DATA.topic(dataId), subscriber);
    }

    public void unsubscribeFromData(String dataId, Subscriber subscriber) {
        this.unsubscribe(PostingType.DATA.topic(dataId), subscriber);
    }

    public void subscribeForStatus(Subscriber subscriber) {
        var topic = PostingType.STATUS.topic("");
        this.subscribe(topic, subscriber);
    }

    public void unsubscribeFromStatus(Subscriber subscriber) {
        var topic = PostingType.STATUS.topic("");
        this.unsubscribe(topic, subscriber);
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