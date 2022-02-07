package de.ude.es;

import de.ude.es.comm.CommunicationEndpoint;
import de.ude.es.comm.Posting;
import de.ude.es.comm.Protocol;
import de.ude.es.comm.Subscriber;

public class HeartbeatSubscriber {

    private static class DataSubscriber implements Subscriber {
        TwinList twinList;

        DataSubscriber(TwinList twinList) {
            this.twinList = twinList;
        }

        @Override
        public void deliver(Posting posting) {
            String topic = posting.topic();
            System.out.println("POSTING: " + topic);
            String[] topicSplit = topic.split("/");
            twinList.addTwin(topicSplit[topicSplit.length - 2]);
        }
    }

    private Protocol protocol;
    private DataSubscriber subscriber;

    public HeartbeatSubscriber(CommunicationEndpoint endpoint, TwinList twinList) {
        this.protocol = new Protocol(endpoint);
        this.subscriber = new DataSubscriber(twinList);
        this.protocol.subscribeForHeartbeat("eip://uni-due.de/es/+", subscriber);
    }

//    public void unbind() {
//        protocol.unsubscribeFromData(dataId, subscriber);
//        protocol.publishDataStopRequest(dataId, localId);
//    }

}
