package org.ude.es.twinImplementations;


import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

import java.util.ArrayList;
import java.util.List;


public class PowerConsumptionTwin extends JavaTwin {

    private static final int WAIT_AFTER_COMMAND = 1000;
    private final TwinStub enV5;
    public ValueClass wifiValueReceiver;
    public ValueClass sRamValueReceiver;
    private boolean deviceOnline = false;
    private final List<String> openDataRequests = new ArrayList<>();

    private class StatusReceiver implements Subscriber {

        String identifier;

        public StatusReceiver(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public void deliver(Posting posting) throws InterruptedException {
            String data = posting.data();
            if (data.contains(";1")) {
                System.out.println("Device " + this.identifier + " online.");
                deviceOnline = true;
                for (String request : openDataRequests) {
                    enV5.publishDataStartRequest(request, identifier);
                }
            }

            if (data.contains(";0")) {
                System.out.println("Device " + this.identifier + " offline.");
                deviceOnline = false;
            }
        }
    }

    public PowerConsumptionTwin(String identifier) {
        super(identifier);

        wifiValueReceiver = new ValueClass("wifiValue");
        sRamValueReceiver = new ValueClass("sRamValue");

        enV5 = new ENv5TwinStub("enV5");
    }

    private static void waitAfterCommand() {
        try {
            Thread.sleep(WAIT_AFTER_COMMAND);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void executeOnBind() {
        enV5.bindToCommunicationEndpoint(endpoint);
        StatusReceiver statusReceiver = new StatusReceiver(enV5.getDomainAndIdentifier());
        enV5.subscribeForStatus(statusReceiver);
        waitAfterCommand();
    }

    public class ValueClass {
        private float value = -1;
        private long lastTimeReceived;
        private volatile boolean receivedNewValue = false;
        private final String dataID;
        private final ValueReceiver valueReceiver;

        private class ValueReceiver implements Subscriber {
            @Override
            public void deliver(Posting posting) {
                value = Float.parseFloat(posting.data());
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
                enV5.publishDataStartRequest(dataID, getDomainAndIdentifier());
                waitAfterCommand();
            }
            openDataRequests.add(dataID);
            enV5.subscribeForData(dataID, valueReceiver);
        }

        public void stopRequestingData() {
            if (deviceOnline) {
                enV5.publishDataStopRequest(dataID, getDomainAndIdentifier());
            }
            openDataRequests.remove(dataID);
            enV5.unsubscribeFromData(dataID, valueReceiver);
        }

        public float requestValueOnce(float timeOut) {
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

        public float getLastValue() {
            receivedNewValue = false;
            return value;
        }

        public long receivedWhen() {
            return lastTimeReceived;
        }
    }
}
