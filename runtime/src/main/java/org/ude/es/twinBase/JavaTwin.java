package org.ude.es.twinBase;

import org.ude.es.comm.Posting;
import org.ude.es.comm.PostingType;
import org.ude.es.comm.Subscriber;

import java.util.*;

public class JavaTwin extends Twin {

    public JavaTwin(String identifier) {
        super(identifier);
    }

    public class DataRequestReceiver {

        private final String dataID;
        DataRequesterInterface dataRequester;
        HashMap<String, TwinStub> subscribers = new HashMap<>();
        private final DataStartRequestReceiver dataStartRequestReceiver = new DataStartRequestReceiver();
        private final DataStopRequestReceiver dataStopRequestReceiver = new DataStopRequestReceiver();
        private boolean dataRequested;
        Thread dataThread;

        private class DataStartRequestReceiver implements Subscriber {
            @Override
            public void deliver(Posting posting) {
                TwinStub stub = new TwinStub(posting.data(), new TwinStub.StatusInterface() {
                    @Override
                    public void deviceGoesOnline() {

                    }

                    @Override
                    public void deviceGoesOffline() {
                        dataStopRequestReceiver.deliver(new Posting("", posting.data()));
                    }
                });
                stub.bindToCommunicationEndpoint(endpoint);
                subscribers.put(posting.data(), stub);

                if (subscribers.size() == 1) {
                    dataRequester.startRequestingData();
                    dataThread = new Thread(() -> {
                        dataRequested = true;
                        while (dataRequested) {
                            try {
                                if (dataRequester.receivedNewValue()) {
                                    publishData(dataID, dataRequester.getLastValue());
                                }
                                Thread.sleep(100);
                            } catch (InterruptedException v) {
                                System.out.println(v);
                            }
                        }
                    });
                    dataThread.start();
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
                    dataRequester.stopRequestingData();
                    dataRequested = false;
                }
            }
        }

        public DataRequestReceiver(String dataID, DataRequesterInterface dataRequester) {
            this.dataRequester = dataRequester;
            this.dataID = dataID;
            subscribeForDataStartRequest(dataID, dataStartRequestReceiver);
            subscribeForDataStopRequest(dataID, dataStopRequestReceiver);
        }
    }

    public void publishData(String dataId, String value) {
        this.publish(Posting.createData(dataId, value));
    }

    public void publishStatus(boolean online) {
        this.publish(Posting.createStatus(this.identifier, online));
    }

    public void subscribeForDataStartRequest(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.START.topic(dataId), subscriber);
    }

    public void unsubscribeFromDataStartRequest(String dataId, Subscriber subscriber) {
        this.unsubscribe(PostingType.START.topic(dataId), subscriber);
    }

    public void subscribeForDataStopRequest(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.STOP.topic(dataId), subscriber);
    }

    public void unsubscribeFromDataStopRequest(String dataId, Subscriber subscriber) {
        this.unsubscribe(PostingType.STOP.topic(dataId), subscriber);
    }

    public void subscribeForCommand(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.SET.topic(dataId), subscriber);
    }

    public void unsubscribeFromCommand(String dataId, Subscriber subscriber) {
        this.unsubscribe(PostingType.SET.topic(dataId), subscriber);
    }
}
