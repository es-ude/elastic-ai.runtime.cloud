package org.ude.es.protocol;

import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataRequestHandler {

    private static final HashMap<String, RequesterTwinStub> currentlyRequestingTwins = new HashMap<>();

    private final List<String> subscribers = new ArrayList<>();

    private final List<TwinStub.Executor> startRequestingData = new ArrayList<>();
    private final List<TwinStub.Executor> stopRequestingData = new ArrayList<>();

    private final DataStopRequestReceiver dataStopRequestReceiver = new DataStopRequestReceiver();

    private final String dataID;
    private final JavaTwin twinWithData;

    public DataRequestHandler(JavaTwin twinWithData, String dataID) {
        this.twinWithData = twinWithData;
        this.dataID = dataID;
        DataStartRequestReceiver dataStartRequestReceiver = new DataStartRequestReceiver();
        twinWithData.subscribeForDataStartRequest(dataID, dataStartRequestReceiver);
        twinWithData.subscribeForDataStopRequest(dataID, dataStopRequestReceiver);
    }

    public void newDataToPublish(String data) {
        twinWithData.publishData(dataID, data);
    }

    public void addWhenStartRequestingData(TwinStub.Executor function) {
        startRequestingData.add(function);
    }

    public void addWhenStopRequestingData(TwinStub.Executor function) {
        stopRequestingData.add(function);
    }

    private class DataStartRequestReceiver implements Subscriber {
        @Override
        public void deliver(Posting posting) {
            String requesterID = posting.data();
            if (alreadySubscribed(requesterID))
                return;

            handleRequesterTwinStub(requesterID);
            stopWhenDeviceGoesOffline(requesterID);
            handleSubscribers(requesterID);
        }

        private boolean alreadySubscribed(String requesterID) {
            return subscribers.contains(requesterID);
        }

        private void handleSubscribers(String requesterID) {
            subscribers.add(requesterID);
            if (subscribers.size() == 1) {
                for (TwinStub.Executor executor : startRequestingData) {
                    executor.execute();
                }
            }
        }

        private void handleRequesterTwinStub(String requesterID) {
            if (!currentlyRequestingTwins.containsKey(requesterID)) {
                RequesterTwinStub stub = new RequesterTwinStub(requesterID);
                twinWithData.bindStub(stub);
                currentlyRequestingTwins.put(requesterID, stub);
            } else {
                currentlyRequestingTwins.get(requesterID).newSubscriber();
            }
        }
    }

    private void stopWhenDeviceGoesOffline(String requesterID) {
        currentlyRequestingTwins.get(requesterID).addWhenDeviceGoesOffline(
                () -> dataStopRequestReceiver.deliver(new Posting("", requesterID)));
    }

    private class DataStopRequestReceiver implements Subscriber {
        @Override
        public void deliver(Posting posting) {
            String requesterID = posting.data();
            if (notSubscribed(requesterID))
                return;

            handleSubscriber(requesterID);
            handleRequesterTwinStub(requesterID);
        }

        private boolean notSubscribed(String requesterID) {
            return !subscribers.contains(requesterID);
        }

        private void handleRequesterTwinStub(String requesterID) {
            currentlyRequestingTwins.get(requesterID).subscriberLeaves();
            if (!currentlyRequestingTwins.get(requesterID).hasSubscriber()) {
                currentlyRequestingTwins.get(requesterID).unsubscribeFromStatus(dataStopRequestReceiver);
                currentlyRequestingTwins.remove(requesterID);
            }
        }

        private void handleSubscriber(String requesterID) {
            subscribers.remove(requesterID);
            if (subscribers.size() == 0) {
                for (TwinStub.Executor executor : stopRequestingData) {
                    executor.execute();
                }
            }
        }
    }
}
