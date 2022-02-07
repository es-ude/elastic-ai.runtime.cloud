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
            twinList.addTwin(posting.data());
        }
    }

    private Protocol protocol;
    private DataSubscriber subscriber;

    public HeartbeatSubscriber(TwinList twinList) {
        this.subscriber = new DataSubscriber(twinList);
    }

    public void bind(CommunicationEndpoint endpoint) {
        bind(new Protocol(endpoint));
    }

    public void bind(Protocol protocol) {
        this.protocol = protocol;
        this.protocol.subscribeForHeartbeat("eip://uni-due.de/es/+", subscriber);
    }

//    public void unbind() {
//        protocol.unsubscribeFromData(dataId, subscriber);
//        protocol.publishDataStopRequest(dataId, localId);
//    }

}
