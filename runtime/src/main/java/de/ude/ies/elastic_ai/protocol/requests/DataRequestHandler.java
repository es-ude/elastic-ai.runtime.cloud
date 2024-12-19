package de.ude.ies.elastic_ai.protocol.requests;

import de.ude.ies.elastic_ai.communicationEndpoints.LocalCommunicationEndpoint;
import de.ude.ies.elastic_ai.communicationEndpoints.RemoteCommunicationEndpoint;
import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.Subscriber;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataRequestHandler {

    private static final HashMap<String, RequesterRemoteCE> currentlyRequestingTwins =
        new HashMap<>();

    private final List<String> subscribers = new ArrayList<>();

    private final List<RemoteCommunicationEndpoint.Executor> startRequestingData =
        new ArrayList<>();
    private final List<RemoteCommunicationEndpoint.Executor> stopRequestingData = new ArrayList<>();

    private final DataStopRequestReceiver dataStopRequestReceiver = new DataStopRequestReceiver();

    private final String dataID;
    private final LocalCommunicationEndpoint endpointWithData;

    public DataRequestHandler(LocalCommunicationEndpoint endpointWithData, String dataID) {
        this.endpointWithData = endpointWithData;
        this.dataID = dataID;
        DataStartRequestReceiver dataStartRequestReceiver = new DataStartRequestReceiver();
        endpointWithData.subscribeForDataStartRequest(dataID, dataStartRequestReceiver);
        endpointWithData.subscribeForDataStopRequest(dataID, dataStopRequestReceiver);
    }

    public void stop() {
        endpointWithData.unsubscribeFromDataStartRequest(dataID);
        endpointWithData.unsubscribeFromDataStopRequest(dataID);
    }

    public void newDataToPublish(String data) {
        endpointWithData.publishData(dataID, data);
    }

    public void addWhenStartRequestingData(RemoteCommunicationEndpoint.Executor function) {
        startRequestingData.add(function);
    }

    public void addWhenStopRequestingData(RemoteCommunicationEndpoint.Executor function) {
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
                endpointWithData.bindStub(stub);
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
                currentlyRequestingTwins.get(requesterID).unsubscribeFromStatus();
                currentlyRequestingTwins.remove(requesterID);
            }
        }

        private void handleSubscriber(String requesterID) {
            subscribers.remove(requesterID);
            if (subscribers.isEmpty()) {
                for (RemoteCommunicationEndpoint.Executor executor : stopRequestingData) {
                    executor.execute();
                }
            }
        }
    }
}
