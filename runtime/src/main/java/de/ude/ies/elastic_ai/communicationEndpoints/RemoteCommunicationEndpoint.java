package de.ude.ies.elastic_ai.communicationEndpoints;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.PostingType;
import de.ude.ies.elastic_ai.protocol.Status;
import de.ude.ies.elastic_ai.protocol.Subscriber;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RemoteCommunicationEndpoint extends CommunicationEndpoint {

    private final int deviceDelay;
    private boolean clientOnline = false;
    Lock lock = new ReentrantLock();

    List<DataExecutor> deviceGoesOnline = new ArrayList<>();
    List<DataExecutor> deviceGoesOffline = new ArrayList<>();

    public RemoteCommunicationEndpoint(String identifier, int deviceDelay) {
        super(identifier);
        this.deviceDelay = deviceDelay;
    }

    public RemoteCommunicationEndpoint(String identifier) {
        this(identifier, 0);
    }

    public void addWhenDeviceGoesOnline(DataExecutor function) {
        deviceGoesOnline.add(function);
    }

    public void addWhenDeviceGoesOffline(DataExecutor function) {
        deviceGoesOffline.add(function);
    }

    private class StatusReceiver implements Subscriber {

        String identifier;

        public StatusReceiver(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public void deliver(Posting posting) {
            String data = posting.data();
            List<DataExecutor> tmpExecutor = new ArrayList<>();
            if (data.contains(Status.State.ONLINE.toString())) {
                clientOnline = true;
                tmpExecutor = new ArrayList<>(deviceGoesOnline);
            }

            if (data.contains(Status.State.OFFLINE.toString())) {
                clientOnline = false;
                tmpExecutor = new ArrayList<>(deviceGoesOffline);
            }

            for (DataExecutor executor : tmpExecutor) {
                executor.execute(posting.data());
            }
        }
    }

    protected void executeOnBindPrivate() {
        super.executeOnBindPrivate();
        StatusReceiver statusReceiver = new StatusReceiver(getDomainAndIdentifier());

        if (!this.broker.isConnected()) {
            this.broker.connect(this.identifier, null);
        }

        subscribeForStatus(statusReceiver);
    }

    public boolean isOnline() {
        return clientOnline;
    }

    public void subscribeForData(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.DATA.topic(dataId), subscriber);
    }

    public void unsubscribeFromData(String dataId) {
        this.unsubscribe(PostingType.DATA.topic(dataId));
    }

    public void subscribeForDone(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.DONE.topic(dataId), subscriber);
    }

    public void unsubscribeFromDone(String dataId) {
        this.unsubscribe(PostingType.DONE.topic(dataId));
    }

    public void subscribeForStatus(Subscriber subscriber) {
        var topic = PostingType.STATUS.topic("");
        this.subscribe(topic, subscriber);
    }

    public void unsubscribeFromStatus() {
        var topic = PostingType.STATUS.topic("");
        this.unsubscribe(topic);
    }

    @Override
    protected void publish(Posting posting) {
        try {
            if (lock.tryLock(deviceDelay * 10L, MILLISECONDS)) {
                super.publish(posting);
                waitAfterPublish();
                lock.unlock();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitAfterPublish() {
        try {
            Thread.sleep(deviceDelay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
}
