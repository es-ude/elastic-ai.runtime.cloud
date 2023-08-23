package org.ude.es.protocol;

import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.Twin;
import org.ude.es.twinBase.TwinStub;

public class DataRequester {

    private final TwinStub twinStub;
    private final String dataID;
    private final String requesterID;
    private final ValueReceiver valueReceiver;
    Twin.DataExecutor dataExecutor;
    private boolean requested = false;
    private boolean blocked = false;

    public DataRequester(TwinStub twinStub, String dataID, String requesterID) {
        this.twinStub = twinStub;
        this.dataID = dataID;
        this.requesterID = requesterID;
        valueReceiver = new ValueReceiver();

        twinStub.addWhenDeviceGoesOnline(data -> getsOnline());
    }

    private void publishStartRequest() {
        if (twinStub.isOnline() && !blocked) {
            twinStub.publishDataStartRequest(dataID, requesterID);
        }
    }

    private void publishStopRequest() {
        if (twinStub.isOnline() && !blocked) {
            twinStub.publishDataStopRequest(dataID, requesterID);
        }
    }

    public void startRequestingData() {
        if (requested) return;
        requested = true;
        twinStub.subscribeForData(dataID, valueReceiver);
        publishStartRequest();
    }

    public void stopRequestingData() {
        if (!requested) return;
        requested = false;
        twinStub.unsubscribeFromData(dataID);
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

    public void setDataReceiveFunction(Twin.DataExecutor function) {
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
