package org.ude.es.twinBase;

import org.ude.es.comm.Posting;
import org.ude.es.comm.PostingType;
import org.ude.es.comm.Subscriber;

import java.util.ArrayList;
import java.util.List;

public class TwinStub extends Twin {

    private final int deviceDelay;
    public boolean deviceOnline = false;
    public final List<String> openDataRequests = new ArrayList<>();

    public interface StatusInterface {
        void deviceGoesOnline();

        void deviceGoesOffline();
    }

    StatusInterface statusInterface;


    public TwinStub(String identifier, StatusInterface statusInterface, int deviceDelay) {
        super(identifier);
        this.statusInterface = statusInterface;
        this.deviceDelay = deviceDelay;
    }

    public TwinStub(String identifier, StatusInterface statusInterface) {
        this(identifier, statusInterface, 0);
    }

    public TwinStub(String identifier) {
        this(identifier, new StatusInterface() {
            @Override
            public void deviceGoesOnline() {

            }

            @Override
            public void deviceGoesOffline() {

            }
        }, 0);
    }

    private class StatusReceiver implements Subscriber {

        String identifier;

        public StatusReceiver(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public void deliver(Posting posting) throws InterruptedException {
            String data = posting.data();
            if (data.contains(";1")) {
                deviceOnline = true;
                for (String request : openDataRequests) {
                    publishDataStartRequest(request, identifier);
                }

                statusInterface.deviceGoesOnline();
            }

            if (data.contains(";0")) {
                deviceOnline = false;

                statusInterface.deviceGoesOffline();
            }
        }
    }

    public class DataRequester implements DataRequesterInterface {
        private String value = "";
        private volatile boolean receivedNewValue = false;
        private final String dataID;
        private final String requesterID;
        private final ValueReceiver valueReceiver;

        private class ValueReceiver implements Subscriber {
            @Override
            public void deliver(Posting posting) {
                value = posting.data();
                receivedNewValue = true;
            }
        }

        public DataRequester(String dataID, String requesterID) {
            this.dataID = dataID;
            this.requesterID = requesterID;
            valueReceiver = new ValueReceiver();
        }

        @Override
        public void startRequestingData() {
            if (deviceOnline) {
                publishDataStartRequest(dataID, requesterID);
                waitAfterCommand();
            }
            openDataRequests.add(dataID);
            subscribeForData(dataID, valueReceiver);
        }

        @Override
        public void stopRequestingData() {
            if (deviceOnline) {
                publishDataStopRequest(dataID, requesterID);
                waitAfterCommand();
            }
            openDataRequests.remove(dataID);
            unsubscribeFromData(dataID, valueReceiver);
        }

        @Override
        public boolean receivedNewValue() {
            return receivedNewValue;
        }

        @Override
        public String getLastValue() {
            receivedNewValue = false;
            return value;
        }
    }

    @Override
    public void executeOnBindPrivate() {
        StatusReceiver statusReceiver = new StatusReceiver(getDomainAndIdentifier());
        subscribeForStatus(statusReceiver);
        executeOnBind();
    }

    private void waitAfterCommand() {
        try {
            Thread.sleep(deviceDelay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public DataRequester newDataRequester(String wifiValue, String requesterID) {
        return new DataRequester(wifiValue, requesterID);
    }

    public void subscribeForData(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.DATA.topic(dataId), subscriber);
    }

    public void unsubscribeFromData(String dataId, Subscriber subscriber) {
        this.unsubscribe(PostingType.DATA.topic(dataId), subscriber);
    }

    public void subscribeForStatus(Subscriber subscriber) {
        var topic = PostingType.STATUS.topic("");
        this.subscribe(topic, subscriber);
    }

    public void unsubscribeFromStatus(Subscriber subscriber) {
        var topic = PostingType.STATUS.topic("");
        this.unsubscribe(topic, subscriber);
    }

    public void publishDataStartRequest(String dataId, String receiver) {
        Posting post = Posting.createStartSending(dataId, receiver);
        this.publish(post);
    }

    public void publishDataStopRequest(String dataId, String receiver) {
        Posting post = Posting.createStopSending(dataId, receiver);
        this.publish(post);
    }

    public void publishCommand(String service, String cmd) {
        Posting post = Posting.createCommand(service, cmd);
        this.publish(post);
    }

    public void publishOnCommand(String service) {
        Posting post = Posting.createTurnOn(service);
        this.publish(post);
    }

    public void publishOffCommand(String service) {
        Posting post = Posting.createTurnOff(service);
        this.publish(post);
    }
}
