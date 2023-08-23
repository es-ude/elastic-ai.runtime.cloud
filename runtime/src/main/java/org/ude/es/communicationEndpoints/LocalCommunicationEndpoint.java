package org.ude.es.communicationEndpoints;

import org.ude.es.protocol.Posting;
import org.ude.es.protocol.PostingType;
import org.ude.es.protocol.Status;
import org.ude.es.protocol.Subscriber;

public class LocalCommunicationEndpoint extends CommunicationEndpoint {

    public LocalCommunicationEndpoint(String identifier) {
        super(identifier);
    }

    protected final Status minimalStatus = new Status(this.identifier)
        .append(Status.Parameter.TYPE.value(Status.Type.TWIN.get()))
        .append(Status.Parameter.STATE.value(Status.State.ONLINE.get()));

    protected void executeOnBindPrivate() {
        Status lwtMessage = new Status(this.identifier)
            .append(Status.Parameter.TYPE.value(Status.Type.TWIN.get()))
            .append(Status.Parameter.STATE.value(Status.State.OFFLINE.get()));
        this.brokerStub.connect(this.identifier, lwtMessage.get());
        publishStatus(minimalStatus);
        super.executeOnBindPrivate();
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

    public void subscribeForDataStartRequest(
        String dataId,
        Subscriber subscriber
    ) {
        this.subscribe(PostingType.START.topic(dataId), subscriber);
    }

    public void unsubscribeFromDataStartRequest(String dataId) {
        this.unsubscribe(PostingType.START.topic(dataId));
    }

    public void subscribeForDataStopRequest(
        String dataId,
        Subscriber subscriber
    ) {
        this.subscribe(PostingType.STOP.topic(dataId), subscriber);
    }

    public void unsubscribeFromDataStopRequest(String dataId) {
        this.unsubscribe(PostingType.STOP.topic(dataId));
    }

    public void subscribeForCommand(String dataId, Subscriber subscriber) {
        System.out.println(dataId);
        this.subscribe(PostingType.COMMAND.topic(dataId), subscriber);
    }

    public void unsubscribeFromCommand(String dataId) {
        this.unsubscribe(PostingType.COMMAND.topic(dataId));
    }

    public void bindStub(RemoteCommunicationEndpoint stub) {
        stub.bindToCommunicationEndpoint(brokerStub);
    }
}
