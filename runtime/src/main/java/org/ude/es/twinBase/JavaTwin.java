package org.ude.es.twinBase;

import org.ude.es.comm.Posting;
import org.ude.es.comm.PostingType;
import org.ude.es.comm.Status;
import org.ude.es.comm.Subscriber;

public class JavaTwin extends Twin {

    public JavaTwin(String identifier) {
        super(identifier);
    }

    protected final Status minimalStatus = new Status(this.identifier).Type("TWIN").State("ONLINE");

    @Override
    protected void executeOnBindPrivate() {
        Status lwtMessage = new Status(this.identifier).Type("TWIN").State("OFFLINE");
        this.endpoint.connect(this.identifier, lwtMessage.create());
        publishStatus(minimalStatus);
        executeOnBind();
    }

    public void publishData(String dataId, String value) {
        this.publish(Posting.createData(dataId, value));
    }

    public void publishStatus(Status status) {
        this.publish(Posting.createStatus(status.create()), true);
    }

    public void subscribeForDataStartRequest(
            String dataId,
            Subscriber subscriber
    ) {
        this.subscribe(PostingType.START.topic(dataId), subscriber);
    }

    public void unsubscribeFromDataStartRequest(
            String dataId,
            Subscriber subscriber
    ) {
        this.unsubscribe(PostingType.START.topic(dataId), subscriber);
    }

    public void subscribeForDataStopRequest(
            String dataId,
            Subscriber subscriber
    ) {
        this.subscribe(PostingType.STOP.topic(dataId), subscriber);
    }

    public void unsubscribeFromDataStopRequest(
            String dataId,
            Subscriber subscriber
    ) {
        this.unsubscribe(PostingType.STOP.topic(dataId), subscriber);
    }

    public void subscribeForCommand(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.SET.topic(dataId), subscriber);
    }

    public void unsubscribeFromCommand(String dataId, Subscriber subscriber) {
        this.unsubscribe(PostingType.SET.topic(dataId), subscriber);
    }

    public void bindStub(TwinStub stub) {
        stub.bindToCommunicationEndpoint(endpoint);
    }
}
