package de.ude.ies.elastic_ai.protocol.requests;

import de.ude.ies.elastic_ai.communicationEndpoints.CommunicationEndpoint;
import de.ude.ies.elastic_ai.communicationEndpoints.RemoteCommunicationEndpoint;
import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.Subscriber;

public class DataRequester {

    private final RemoteCommunicationEndpoint remoteCommunicationEndpoint;
    private final String dataID;
    private final String requesterID;
    CommunicationEndpoint.DataExecutor dataExecutor;
    private boolean requested = false;
    private boolean listening = false;

    public DataRequester(
        RemoteCommunicationEndpoint remoteCommunicationEndpoint,
        String dataID,
        String requesterID
    ) {
        this.remoteCommunicationEndpoint = remoteCommunicationEndpoint;
        this.dataID = dataID;
        this.requesterID = requesterID;
        ValueReceiver valueReceiver = new ValueReceiver();

        remoteCommunicationEndpoint.subscribeForData(dataID, valueReceiver);
        remoteCommunicationEndpoint.addWhenDeviceGoesOnline(data -> getsOnline());
    }

    private void publishStartRequest() {
        if (remoteCommunicationEndpoint.isOnline()) {
            remoteCommunicationEndpoint.publishDataStartRequest(dataID, requesterID);
        }
    }

    private void publishStopRequest() {
        if (remoteCommunicationEndpoint.isOnline()) {
            remoteCommunicationEndpoint.publishDataStopRequest(dataID, requesterID);
        }
    }

    public void startRequestingData() {
        if (requested) return;
        requested = true;
        publishStartRequest();
        listenToData(true);
    }

    public void stopRequestingData() {
        if (!requested) return;
        requested = false;
        publishStopRequest();
        listenToData(false);
    }

    public void setDataReceiveFunction(CommunicationEndpoint.DataExecutor function) {
        dataExecutor = function;
    }

    private void getsOnline() {
        if (requested) {
            publishStartRequest();
        }
    }

    public void listenToData(boolean listen) {
        this.listening = listen;
    }

    private class ValueReceiver implements Subscriber {

        @Override
        public void deliver(Posting posting) {
            if (listening) {
                dataExecutor.execute(posting.data());
            }
        }
    }
}
