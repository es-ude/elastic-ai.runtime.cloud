package org.ude.es.twinBase;

import org.ude.es.comm.Posting;
import org.ude.es.comm.PostingType;
import org.ude.es.comm.Subscriber;

public class JavaTwin extends Twin {

    public JavaTwin(String identifier) {
        super(identifier);
    }

    @Override
    protected void executeOnBindPrivate() {
        String lwtMessage = "ID:" + this.identifier + ";TYPE:TWIN;STATUS:OFFLINE;";
        this.endpoint.connect(this.identifier, lwtMessage);
        publishStatus("");
        executeOnBind();
    }

    public void publishData(String dataId, String value) {
        this.publish(Posting.createData(dataId, value));
    }

    public void publishStatus(String information) {
        this.publish(Posting.createStatus("ID:" + this.identifier + ";TYPE:TWIN;STATUS:ONLINE;" + information), true);
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
