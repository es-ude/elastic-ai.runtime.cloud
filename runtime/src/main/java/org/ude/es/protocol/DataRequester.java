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
    private boolean requested = false;
    private boolean dataWasBlocked = false;
    private boolean blocked = false;

    Twin.DataExecutor dataExecutor;

    private class ValueReceiver implements Subscriber {

        @Override
        public void deliver(Posting posting) {
            dataExecutor.execute(posting.data());
        }
    }

    public DataRequester(TwinStub twinStub, String dataID, String requesterID) {
        this.twinStub = twinStub;
        this.dataID = dataID;
        this.requesterID = requesterID;
        valueReceiver = new ValueReceiver();

        twinStub.addWhenDeviceGoesOnline(data -> getsOnline());
    }

    public void pauseDataRequests() {
        blocked = true;
    }

    public void resumeDataRequests() {
        if (!blocked) return;
        blocked = false;
        if (dataWasBlocked) {
            publishStartStopRequest();
            dataWasBlocked = false;
        }
    }

    private void publishStartStopRequest() {
        if (twinStub.isOnline()) {
            if (requested) twinStub.publishDataStartRequest(
                    dataID,
                    requesterID
            );
            else twinStub.publishDataStopRequest(dataID, requesterID);
            twinStub.waitAfterCommand();
        }
    }

    public void setDataReceiveFunction(Twin.DataExecutor function) {
        dataExecutor = function;
    }

    private void getsOnline() {
        if (requested) {
            twinStub.publishDataStartRequest(dataID, requesterID);
            twinStub.waitAfterCommand();
        }
    }

    public void startRequestingData() {
        if (requested) return;
        requested = true;
        twinStub.subscribeForData(dataID, valueReceiver);
        if (blocked) {
            dataWasBlocked = true;
        } else {
            publishStartStopRequest();
        }
    }

    public void stopRequestingData() {
        if (!requested) return;
        requested = false;
        twinStub.unsubscribeFromData(dataID);
        if (blocked) {
            dataWasBlocked = true;
        } else {
            publishStartStopRequest();
        }
    }
}
