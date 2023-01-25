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

    public interface DataExecuter {
        void execute(String data);
    }

    List<DataExecuter> dataExecuter = new ArrayList<>();

    private class ValueReceiver implements Subscriber {
        @Override
        public void deliver(Posting posting) {
            for (DataExecuter executer : dataExecuter) {
                executer.execute(posting.data());
            }
        }
    }

    public void addWhenNewDataReceived(DataExecuter function) {
        dataExecuter.add(function);
    }

    public DataRequester(TwinStub twinStub, String dataID, String requesterID) {
        this.twinStub = twinStub;
        this.dataID = dataID;
        this.requesterID = requesterID;
        valueReceiver = new ValueReceiver();

        twinStub.addWhenDeviceGoesOnline(() -> {
            for (String request : openDataRequests) {
                twinStub.publishDataStartRequest(request, requesterID);
                twinStub.waitAfterCommand();
            }
        });
    }

    public void startRequestingData() {
        if (twinStub.isOnline()) {
            twinStub.publishDataStartRequest(dataID, requesterID);
            twinStub.waitAfterCommand();
        }
        openDataRequests.add(dataID);
        twinStub.subscribeForData(dataID, valueReceiver);
    }

    public void stopRequestingData() {
        if (twinStub.isOnline()) {
            twinStub.publishDataStopRequest(dataID, requesterID);
            twinStub.waitAfterCommand();
        }
        openDataRequests.remove(dataID);
        twinStub.unsubscribeFromData(dataID, valueReceiver);
    }
}
