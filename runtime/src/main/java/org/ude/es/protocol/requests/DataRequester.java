package org.ude.es.protocol.requests;

import org.ude.es.communicationEndpoints.CommunicationEndpoint;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;
import org.ude.es.protocol.Posting;
import org.ude.es.protocol.Subscriber;

public class DataRequester {

    private final RemoteCommunicationEndpoint remoteCommunicationEndpoint;
    private final String dataID;
    private final String requesterID;
    private final ValueReceiver valueReceiver;
    CommunicationEndpoint.DataExecutor dataExecutor;
    private boolean requested = false;
    private boolean blocked = false;

    public DataRequester(
        RemoteCommunicationEndpoint remoteCommunicationEndpoint,
        String dataID,
        String requesterID
    ) {
        this.remoteCommunicationEndpoint = remoteCommunicationEndpoint;
        this.dataID = dataID;
        this.requesterID = requesterID;
        valueReceiver = new ValueReceiver();

        remoteCommunicationEndpoint.addWhenDeviceGoesOnline(data -> getsOnline()
        );
    }

    private void publishStartRequest() {
        if (remoteCommunicationEndpoint.isOnline() && !blocked) {
            remoteCommunicationEndpoint.publishDataStartRequest(
                dataID,
                requesterID
            );
        }
    }

    private void publishStopRequest() {
        if (remoteCommunicationEndpoint.isOnline() && !blocked) {
            remoteCommunicationEndpoint.publishDataStopRequest(
                dataID,
                requesterID
            );
        }
    }

    public void startRequestingData() {
        if (requested) return;
        requested = true;
        remoteCommunicationEndpoint.subscribeForData(dataID, valueReceiver);
        publishStartRequest();
    }

    public void stopRequestingData() {
        if (!requested) return;
        requested = false;
        remoteCommunicationEndpoint.unsubscribeFromData(dataID);
        publishStopRequest();
    }

    public void resumeDataRequests() {
        if (!blocked) return;
        blocked = false;
        if (requested) {
            publishStartRequest();
        }
    }

    public void pauseDataRequests() {
        if (requested) {
            publishStopRequest();
        }
        blocked = true;
    }

    public void setDataReceiveFunction(
        CommunicationEndpoint.DataExecutor function
    ) {
        dataExecutor = function;
    }

    private void getsOnline() {
        if (requested) {
            publishStartRequest();
        }
    }

    private class ValueReceiver implements Subscriber {

        @Override
        public void deliver(Posting posting) {
            dataExecutor.execute(posting.data());
        }
    }
}
