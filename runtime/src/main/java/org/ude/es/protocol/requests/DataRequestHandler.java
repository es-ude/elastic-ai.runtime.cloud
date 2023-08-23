package org.ude.es.protocol.requests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.ude.es.communicationEndpoints.LocalCommunicationEndpoint;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;
import org.ude.es.protocol.Posting;
import org.ude.es.protocol.Subscriber;

public class DataRequestHandler {

    private static final HashMap<
        String,
        RequesterRemoteCE
    > currentlyRequestingTwins = new HashMap<>();

    private final List<String> subscribers = new ArrayList<>();

    private final List<
        RemoteCommunicationEndpoint.Executor
    > startRequestingData = new ArrayList<>();
    private final List<
        RemoteCommunicationEndpoint.Executor
    > stopRequestingData = new ArrayList<>();

    private final DataStopRequestReceiver dataStopRequestReceiver =
        new DataStopRequestReceiver();

    private final String dataID;
    private final LocalCommunicationEndpoint twinWithData;

    public DataRequestHandler(
        LocalCommunicationEndpoint twinWithData,
        String dataID
    ) {
        this.twinWithData = twinWithData;
        this.dataID = dataID;
        DataStartRequestReceiver dataStartRequestReceiver =
            new DataStartRequestReceiver();
        twinWithData.subscribeForDataStartRequest(
            dataID,
            dataStartRequestReceiver
        );
        twinWithData.subscribeForDataStopRequest(
            dataID,
            dataStopRequestReceiver
        );
    }

    public void stop() {
        twinWithData.unsubscribeFromDataStartRequest(dataID);
        twinWithData.unsubscribeFromDataStopRequest(dataID);
    }

    public void newDataToPublish(String data) {
        twinWithData.publishData(dataID, data);
    }

    public void addWhenStartRequestingData(
        RemoteCommunicationEndpoint.Executor function
    ) {
        startRequestingData.add(function);
    }

    public void addWhenStopRequestingData(
        RemoteCommunicationEndpoint.Executor function
    ) {
        stopRequestingData.add(function);
    }

    private class DataStartRequestReceiver implements Subscriber {

        @Override
        public void deliver(Posting posting) {
            String requesterID = posting.data();
            if (alreadySubscribed(requesterID)) return;

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
                for (RemoteCommunicationEndpoint.Executor executor : startRequestingData) {
                    executor.execute();
                }
            }
        }

        private void handleRequesterTwinStub(String requesterID) {
            if (!currentlyRequestingTwins.containsKey(requesterID)) {
                RequesterRemoteCE stub = new RequesterRemoteCE(requesterID);
                twinWithData.bindStub(stub);
                currentlyRequestingTwins.put(requesterID, stub);
            } else {
                currentlyRequestingTwins.get(requesterID).newSubscriber();
            }
        }
    }

    private void stopWhenDeviceGoesOffline(String requesterID) {
        currentlyRequestingTwins
            .get(requesterID)
            .addWhenDeviceGoesOffline(data ->
                dataStopRequestReceiver.deliver(new Posting("", requesterID))
            );
    }

    private class DataStopRequestReceiver implements Subscriber {

        @Override
        public void deliver(Posting posting) {
            String requesterID = posting.data();
            if (notSubscribed(requesterID)) return;

            handleSubscriber(requesterID);
            handleRequesterTwinStub(requesterID);
        }

        private boolean notSubscribed(String requesterID) {
            return !subscribers.contains(requesterID);
        }

        private void handleRequesterTwinStub(String requesterID) {
            currentlyRequestingTwins.get(requesterID).subscriberLeaves();
            if (!currentlyRequestingTwins.get(requesterID).hasSubscriber()) {
                currentlyRequestingTwins
                    .get(requesterID)
                    .unsubscribeFromStatus();
                currentlyRequestingTwins.remove(requesterID);
            }
        }

        private void handleSubscriber(String requesterID) {
            subscribers.remove(requesterID);
            if (subscribers.size() == 0) {
                for (RemoteCommunicationEndpoint.Executor executor : stopRequestingData) {
                    executor.execute();
                }
            }
        }
    }
}
