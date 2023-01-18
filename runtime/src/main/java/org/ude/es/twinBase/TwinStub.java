package org.ude.es.twinBase;

import org.ude.es.comm.Posting;
import org.ude.es.comm.PostingType;
import org.ude.es.comm.Subscriber;

import java.util.ArrayList;
import java.util.List;

public class TwinStub extends Twin {

    private final int deviceDelay;
    public boolean deviceOnline = false;

    List<FunctionalInterface> deviceGoesOnline = new ArrayList<>();

    List<FunctionalInterface> deviceGoesOffline = new ArrayList<>();

    public TwinStub(String identifier, int deviceDelay) {
        super(identifier);
        this.deviceDelay = deviceDelay;
    }

    public TwinStub(String identifier) {
        this(identifier, 0);
    }

    public void addWhenDeviceGoesOnline(FunctionalInterface function) {
        deviceGoesOnline.add(function);
    }

    public void addWhenDeviceGoesOffline(FunctionalInterface function) {
        deviceGoesOffline.add(function);
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

                for (FunctionalInterface function : deviceGoesOnline) {
                    function.function();
                }
            }

            if (data.contains(";0")) {
                deviceOnline = false;

                for (FunctionalInterface function : deviceGoesOffline) {
                    function.function();
                }
            }
        }
    }

    @Override
    public void executeOnBindPrivate() {
        StatusReceiver statusReceiver = new StatusReceiver(getDomainAndIdentifier());
        subscribeForStatus(statusReceiver);
        executeOnBind();
    }

    public void waitAfterCommand() {
        try {
            Thread.sleep(deviceDelay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
        this.publish(post, false);
    }

    public void publishDataStopRequest(String dataId, String receiver) {
        Posting post = Posting.createStopSending(dataId, receiver);
        this.publish(post, false);
    }

    public void publishCommand(String service, String cmd) {
        Posting post = Posting.createCommand(service, cmd);
        this.publish(post, false);
    }

    public void publishOnCommand(String service) {
        Posting post = Posting.createTurnOn(service);
        this.publish(post, false);
    }

    public void publishOffCommand(String service) {
        Posting post = Posting.createTurnOff(service);
        this.publish(post, false);
    }
}
