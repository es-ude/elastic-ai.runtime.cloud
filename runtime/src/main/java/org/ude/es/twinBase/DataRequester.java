package org.ude.es.twinBase;

import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;

import java.util.ArrayList;
import java.util.List;

public class DataRequester {
    private final TwinStub twinStub;
    private final String dataID;
    private final String requesterID;
    private final ValueReceiver valueReceiver;
    public final List<String> openDataRequests = new ArrayList<>();

    public interface NewDataReceived {
        void function(String data);
    }

    List<NewDataReceived> newDataReceived = new ArrayList<>();

    private class ValueReceiver implements Subscriber {
        @Override
        public void deliver(Posting posting) {
            for (NewDataReceived function : newDataReceived) {
                function.function(posting.data());
            }
        }
    }

    public void addWhenNewDataReceived(NewDataReceived function) {
        newDataReceived.add(function);
    }

    public DataRequester(TwinStub twinStub, String dataID, String requesterID) {
        this.twinStub = twinStub;
        this.dataID = dataID;
        this.requesterID = requesterID;
        valueReceiver = new ValueReceiver();

        twinStub.addWhenDeviceGoesOnline(() -> {
            for (String request : openDataRequests) {
                twinStub.publishDataStartRequest(request, requesterID);
            }
        });
    }

    public void startRequestingData() {
        if (twinStub.deviceOnline) {
            twinStub.publishDataStartRequest(dataID, "requesterID");
            twinStub.waitAfterCommand();
        }
        openDataRequests.add(dataID);
        twinStub.subscribeForData(dataID, valueReceiver);
    }

    public void stopRequestingData() {
        if (twinStub.deviceOnline) {
            twinStub.publishDataStopRequest(dataID, requesterID);
            twinStub.waitAfterCommand();
        }
        openDataRequests.remove(dataID);
        twinStub.unsubscribeFromData(dataID, valueReceiver);
    }
}
