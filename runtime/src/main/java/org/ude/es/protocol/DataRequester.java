package org.ude.es.protocol;

import java.util.ArrayList;
import java.util.List;

import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.Twin;
import org.ude.es.twinBase.TwinStub;

public class DataRequester {

    private final TwinStub twinStub;
    private final String dataID;
    private final String requesterID;
    private final ValueReceiver valueReceiver;
    public final List<String> openDataRequests = new ArrayList<>();

    private boolean blocked = false;

    List<Twin.DataExecutor> dataExecutor = new ArrayList<>();

    private class ValueReceiver implements Subscriber {
        @Override
        public void deliver(Posting posting) {
            for (Twin.DataExecutor executor : dataExecutor) {
                executor.execute(posting.data());
            }
        }
    }

    public void pauseDataRequests() {
        blocked = true;
    }

    public void resumeDataRequests() {
        blocked = false;
        startRequests();
    }

    public void addWhenNewDataReceived(Twin.DataExecutor function) {
        dataExecutor.add(function);
    }

    public DataRequester(TwinStub twinStub, String dataID, String requesterID) {
        this.twinStub = twinStub;
        this.dataID = dataID;
        this.requesterID = requesterID;
        valueReceiver = new ValueReceiver();

        twinStub.addWhenDeviceGoesOnline(data -> startRequests());
    }

    private void startRequests() {
        final List<String> openDataRequestsCopy = new ArrayList<>(
                openDataRequests
        );
        for (String request : openDataRequestsCopy) {
            twinStub.publishDataStartRequest(request, requesterID);
            twinStub.waitAfterCommand();
        }
    }

    public void startRequestingData() {
        if (twinStub.isOnline() && !blocked) {
            twinStub.publishDataStartRequest(dataID, requesterID);
            twinStub.waitAfterCommand();
        }
        openDataRequests.add(dataID);
        twinStub.subscribeForData(dataID, valueReceiver);
    }

    public void stopRequestingData() {
        if (twinStub.isOnline() && !blocked) {
            twinStub.publishDataStopRequest(dataID, requesterID);
            twinStub.waitAfterCommand();
        }
        openDataRequests.remove(dataID);
        twinStub.unsubscribeFromData(dataID);
    }
}
