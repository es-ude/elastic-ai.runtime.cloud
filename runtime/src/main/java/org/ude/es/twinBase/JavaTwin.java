package org.ude.es.twinBase;

import org.ude.es.comm.*;

public class JavaTwin extends Twin {

    public JavaTwin(String identifier) {
        super(identifier);
    }

    protected final Status minimalStatus = new Status(this.identifier)
            .append(Status.Parameter.TYPE.value(Status.Type.TWIN.get()))
            .append(Status.Parameter.STATE.value(Status.State.ONLINE.get()));

    @Override
    protected void executeOnBindPrivate() {
        Status lwtMessage = new Status(this.identifier)
                .append(Status.Parameter.TYPE.value(Status.Type.TWIN.get()))
                .append(Status.Parameter.STATE.value(Status.State.OFFLINE.get()));
        this.endpoint.connect(this.identifier, lwtMessage.get());
        publishStatus(minimalStatus);
        executeOnBind();
    }

    public void publishData(String dataId, String value) {
        this.publish(Posting.createData(dataId, value));
    }

    public void publishStatus(Status status) {
        this.publish(Posting.createStatus(status.get()), true);
    }

    public void publishDone(String command, String value) {
        this.publish(Posting.createDone(command, value));
    }

    public void subscribeForDataStartRequest(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.START.topic(dataId), subscriber);
    }

    public void unsubscribeFromDataStartRequest(String dataId, Subscriber subscriber) {
        this.unsubscribe(PostingType.START.topic(dataId), subscriber);
    }

    public void subscribeForDataStopRequest(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.STOP.topic(dataId), subscriber);
    }

    public void unsubscribeFromDataStopRequest(String dataId, Subscriber subscriber) {
        this.unsubscribe(PostingType.STOP.topic(dataId), subscriber);
    }

    public void subscribeForCommand(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.COMMAND.topic(dataId), subscriber);
    }

    public void unsubscribeFromCommand(String dataId, Subscriber subscriber) {
        this.unsubscribe(PostingType.COMMAND.topic(dataId), subscriber);
    }

    public void bindStub(TwinStub stub) {
        stub.bindToCommunicationEndpoint(endpoint);
    }
}
