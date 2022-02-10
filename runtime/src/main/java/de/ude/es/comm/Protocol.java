package de.ude.es.comm;


/**
 * Encapsulates all knowledge about the actual used protocol, i.e.,
 * how messages are formatted.
 * Should be used by Sources and Sinks as well as your own twins!
 */
public class Protocol {

    // used for subscribing and publishing
    private final CommunicationEndpoint channel;


    public Protocol(CommunicationEndpoint channel) {
        this.channel = channel;
    }

    public void subscribeForData(String dataId, Subscriber subscriber) {
        channel.subscribe(
                PostingType.DATA.topic(dataId),
                subscriber);
    }

    public void unsubscribeFromData(String dataId, Subscriber subscriber) {
        channel.unsubscribe(
                PostingType.DATA.topic(dataId),
                subscriber);
    }

    public void publishData(String dataId, String value) {
        Posting post = Posting.createData(dataId, value);
        channel.publish(post);
    }

    public void subscribeForHeartbeat(String heartbeatSource, Subscriber subscriber) {
        var topic = heartbeatSource+PostingType.HEARTBEAT.topic("");
        channel.subscribeRaw(topic, subscriber);
    }

    public void unsubscribeFromHeartbeat(String heartbeatSource, Subscriber subscriber) {
        var topic = heartbeatSource+PostingType.HEARTBEAT.topic("");
        channel.unsubscribeRaw(topic, subscriber);
    }

    public void publishHeartbeat(String who) {
        Posting post = Posting.createHeartbeat(who);
        channel.publish(post);
    }

    public void subscribeForDataStartRequest(String dataId, Subscriber subscriber) {
        channel.subscribe(
                PostingType.START.topic(dataId),
                subscriber);
    }

    public void subscribeForDataStopRequest(String dataId, Subscriber subscriber) {
        channel.subscribe(
                PostingType.STOP.topic(dataId),
                subscriber);
    }

    public void publishDataStartRequest(String dataId, String receiver) {
        Posting post = Posting.createStartSending(dataId, receiver);
        channel.publish(post);
    }

    public void publishDataStopRequest(String dataId, String receiver) {
        Posting post = Posting.createStopSending(dataId, receiver);
        channel.publish(post);
    }

    public void publishCommand(String service, String cmd) {
        Posting post = Posting.createCommand(service, cmd);
        channel.publish(post);
    }

    public void publishOnCommand(String service) {
        Posting post = Posting.createTurnOn(service);
        channel.publish(post);
    }

    public void publishOffCommand(String service) {
        Posting post = Posting.createTurnOff(service);
        channel.publish(post);
    }

    public String ID() {
        return channel.ID();
    }

    public void subscribeForLost(String client, Subscriber subscriber) {
        var topic = client+PostingType.LOST.topic("");
        channel.subscribe(topic, subscriber);
    }

    public void unsubscribeFromLost(String client, Subscriber subscriber) {
        var topic = client+PostingType.LOST.topic("");
        channel.unsubscribeRaw(topic, subscriber);
    }
}
