package de.ude.es;

import org.ude.es.comm.CommunicationEndpoint;
import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.TwinStub;

public class TwinStatusMonitor {

    private record StatusSubscriber(TwinList twinList) implements Subscriber {
        @Override
        public void deliver(Posting posting) {
            System.out.println(posting.data());

            // retrieve data from status posting
            String twinID = posting.data();
            boolean twinActive = posting.data().equals("1");

            if (twinActive) {
                twinList.addTwin(twinID);
            } else {
                TwinData twin = twinList.getTwin(twinID);
                if (twin != null) {
                    twin.setNotActive();
                }
                // if twin == null -> ignore status message

            }
        }
    }

    private final StatusSubscriber subscriber;
    private final TwinStub twin;

    public TwinStatusMonitor(TwinList twinList) {
        this.subscriber = new StatusSubscriber(twinList);
        this.twin = new TwinStub("+");
    }

    public void bind(CommunicationEndpoint broker) {
        this.twin.bind(broker);
        this.twin.subscribeForStatus(subscriber);
    }
}
