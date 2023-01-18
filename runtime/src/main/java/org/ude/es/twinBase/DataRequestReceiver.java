package org.ude.es.twinBase;

import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataRequestReceiver {

    private final String dataID;
    private final JavaTwin twin;
    List<Twin.FunctionalInterface> startRequestingData = new ArrayList<>();

    List<Twin.FunctionalInterface> stopRequestingData = new ArrayList<>();
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
            stub.bindToCommunicationEndpoint(twin.endpoint);
            subscribers.put(posting.data(), stub);

            if (subscribers.size() == 1) {
                for (Twin.FunctionalInterface function : startRequestingData) {
                    function.function();
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
                for (Twin.FunctionalInterface function : stopRequestingData) {
                    function.function();
                }
            }
        }
    }

    public void addWhenStartRequestingData(Twin.FunctionalInterface function) {
        startRequestingData.add(function);
    }

    public void addWhenStopRequestingData(Twin.FunctionalInterface function) {
        stopRequestingData.add(function);
    }

    public DataRequestReceiver(JavaTwin twin, String dataID) {
        this.twin = twin;
        this.dataID = dataID;
        DataStartRequestReceiver dataStartRequestReceiver = new DataStartRequestReceiver();
        twin.subscribeForDataStartRequest(dataID, dataStartRequestReceiver);
        twin.subscribeForDataStopRequest(dataID, dataStopRequestReceiver);
    }
}
