package org.ude.es.protocol;

import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.Twin;
import org.ude.es.twinBase.TwinStub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataRequestHandler {

    private final String dataID;
    private final JavaTwin twin;
    List<Twin.Executer> startRequestingData = new ArrayList<>();

    List<Twin.Executer> stopRequestingData = new ArrayList<>();
    HashMap<String, TwinStub> subscribers = new HashMap<>();
    private final DataStopRequestReceiver dataStopRequestReceiver = new DataStopRequestReceiver();

    public void newDataToPublish(String data) {
        twin.publishData(dataID, data);
    }

    private class DataStartRequestReceiver implements Subscriber {
        @Override
        public void deliver(Posting posting) {
            TwinStub stub = new TwinStub(posting.data());
            stub.addWhenDeviceGoesOffline(() -> dataStopRequestReceiver.deliver(new Posting("", posting.data())));
            twin.bindStub(stub);
            subscribers.put(posting.data(), stub);

            if (subscribers.size() == 1) {
                for (Twin.Executer executer : startRequestingData) {
                    executer.execute();
                }
            }
        }
    }

    private class DataStopRequestReceiver implements Subscriber {
        @Override
        public void deliver(Posting posting) {
            if (subscribers.get(posting.data()) == null) {
                return;
            }
            subscribers.get(posting.data()).unsubscribeFromStatus(dataStopRequestReceiver);
            subscribers.remove(posting.data());

            if (subscribers.size() == 0) {
                for (Twin.Executer executer : stopRequestingData) {
                    executer.execute();
                }
            }
        }
    }

    public void addWhenStartRequestingData(Twin.Executer function) {
        startRequestingData.add(function);
    }

    public void addWhenStopRequestingData(Twin.Executer function) {
        stopRequestingData.add(function);
    }

    public DataRequestHandler(JavaTwin twin, String dataID) {
        this.twin = twin;
        this.dataID = dataID;
        DataStartRequestReceiver dataStartRequestReceiver = new DataStartRequestReceiver();
        twin.subscribeForDataStartRequest(dataID, dataStartRequestReceiver);
        twin.subscribeForDataStopRequest(dataID, dataStopRequestReceiver);
    }
}
