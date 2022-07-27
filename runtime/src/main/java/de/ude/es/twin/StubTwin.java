package de.ude.es.twin;

import de.ude.es.comm.Posting;
import de.ude.es.comm.PostingType;
import de.ude.es.comm.Subscriber;

public class StubTwin extends Twin {

    public StubTwin(String identifier) {
        super(identifier);
    }

    private void subscribe(String topic, Subscriber subscriber) {
        endpoint.subscribe(identifier + topic, subscriber);
    }

    private void unsubscribe(String topic, Subscriber subscriber) {
        endpoint.unsubscribe(identifier + topic, subscriber);
    }

    private void publish(Posting post) {
        endpoint.publish(post);
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

    public void subscribeForHeartbeat(String heartbeatSource, Subscriber subscriber) {
        var topic = PostingType.HEARTBEAT.topic("");
        this.subscribe(topic, subscriber);
    }

    public void unsubscribeFromHeartbeat(String heartbeatSource, Subscriber subscriber) {
        var topic = PostingType.HEARTBEAT.topic("");
        this.unsubscribe(topic, subscriber);
    }

    public void subscribeForDataStartRequest(String dataId, Subscriber subscriber) {
        this.subscribe(
                PostingType.START.topic(dataId),
                subscriber);
    }

    public void subscribeForDataStopRequest(String dataId, Subscriber subscriber) {
        this.subscribe(
                PostingType.STOP.topic(dataId),
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

    public String ID() {
        return this.ID();
    }

    public void subscribeForLost(Subscriber subscriber) {
        var topic = PostingType.LOST.topic("");
        this.subscribe(topic, subscriber);
    }

    public void unsubscribeFromLost(Subscriber subscriber) {
        var topic = PostingType.LOST.topic("");
        this.unsubscribe(topic, subscriber);
    }

}
