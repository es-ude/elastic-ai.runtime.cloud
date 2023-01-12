package org.ude.es.twinBase;

import org.ude.es.comm.CommunicationEndpoint;
import org.ude.es.comm.Posting;
import org.ude.es.comm.PostingType;
import org.ude.es.comm.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class TwinStub extends Twin {

    private int deviceDelay = 0;

    public       boolean       deviceOnline     = false;
    public final List <String> openDataRequests = new ArrayList <>();

    public TwinStub(String identifier) {
        super(identifier);
    }

    public TwinStub(String identifier, int deviceDelay) {
        super(identifier);
        this.deviceDelay = deviceDelay;
    }

    private void waitAfterCommand() {
        try {
            Thread.sleep(deviceDelay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public ValueClass newValueClass ( String wifiValue ) {
        return new ValueClass( wifiValue );
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
                System.out.println(this.identifier + " online.");
                deviceOnline = true;
                for (String request : openDataRequests) {
                    publishDataStartRequest(request, identifier);
                }
            }

            if (data.contains(";0")) {
                System.out.println(this.identifier + " offline.");
                deviceOnline = false;
            }
        }
    }

    public class ValueClass {
        private String value = "";
        private long   lastTimeReceived;
        private volatile boolean receivedNewValue = false;
        private final String dataID;
        private final ValueReceiver valueReceiver;

        private Function <String, ?> function;
        private class ValueReceiver implements Subscriber {

            @Override
            public void deliver( Posting posting ) {
                value =  posting.data();
                receivedNewValue = true;
                lastTimeReceived = System.currentTimeMillis();
            }
        }

        public ValueClass(String dataID) {
            this.dataID = dataID;
            valueReceiver = new ValueReceiver();
        }

        public void startRequestingData() {
            if (deviceOnline) {
                publishDataStartRequest(dataID, getDomainAndIdentifier());
                waitAfterCommand();
            }
            openDataRequests.add(dataID);
            subscribeForData(dataID, valueReceiver);
        }

        public void stopRequestingData() {
            if (deviceOnline) {
                publishDataStopRequest(dataID, getDomainAndIdentifier());
                waitAfterCommand();
            }
            openDataRequests.remove(dataID);
            unsubscribeFromData(dataID, valueReceiver);
        }

        public Object requestValueOnce ( float timeOut ) {
            receivedNewValue = false;
            long start = System.currentTimeMillis();
            long timeElapsed = 0;
            startRequestingData();
            while (!receivedNewValue && timeElapsed < timeOut) {
                long finish = System.currentTimeMillis();
                timeElapsed = finish - start;
            }
            stopRequestingData();
            return value;
        }

        public boolean receivedNewValue() {
            return receivedNewValue;
        }

        public Object getLastValue () {
            receivedNewValue = false;
            return value;
        }

        public long receivedWhen() {
            return lastTimeReceived;
        }
    }
    @Override
    public void executeOnBindPrivate() {
        StatusReceiver statusReceiver = new StatusReceiver(getDomainAndIdentifier());
        subscribeForStatus(statusReceiver);
        executeOnBind();
    }

    public void subscribeForData ( String dataId, Subscriber subscriber ) {
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
