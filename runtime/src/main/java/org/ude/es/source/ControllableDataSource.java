package org.ude.es.source;

import java.util.ArrayList;
import java.util.List;
import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

/**
 * A simple template class for data sources. Can be used by twins that measure
 * some data to make it available to clients. This class handles listening for
 * START and STOP messages and will subscribe for the heartbeats of all its
 * clients to detect if none of them is available anymore and stop sending
 * updates in that case.
 *
 * @param <T> the type of the measured data
 */
public class ControllableDataSource<T> {

    protected final List<Client> clients = new ArrayList<>();
    protected JavaTwin javaTwin;
    protected final String dataId;

    protected class Client implements Subscriber {

        private final String clientIdentifier;
        private boolean isActive = true;
        private final TwinStub twinStub;

        public Client(String clientIdentifier) {
            this.clientIdentifier = clientIdentifier;
            twinStub = new TwinStub(clientIdentifier);
            twinStub.bind(javaTwin.getEndpoint());
            twinStub.subscribeForStatus(this);
        }

        @Override
        public void deliver(Posting posting) {
            if (posting.data().endsWith("0")) {
                handleLwtReceived();
            }
        }

        private void handleLwtReceived() {
            if (isActive) {
                deactivate();
            }
        }

        public void stopAndRemoveAndUnsubscribe() {
            if (isActive) {
                deactivate();
            }
        }

        private void deactivate() {
            isActive = false;
            clients.remove(this);
            twinStub.unsubscribeFromStatus(this);
        }

        public boolean hasIdentifier(String identifier) {
            return clientIdentifier.equals(identifier);
        }
    }

    public ControllableDataSource(String dataId) {
        this.dataId = dataId;
    }

    public void bind(JavaTwin javaTwin) {
        this.javaTwin = javaTwin;
        javaTwin.subscribeForDataStartRequest(dataId, this::handleNewClient);
        javaTwin.subscribeForDataStopRequest(dataId, this::handleLeavingClient);
    }

    private void handleNewClient(Posting posting) {
        var client = new Client(posting.data());
        clients.add(client);
    }

    private void handleLeavingClient(Posting posting) {
        String leavingClientID = posting.data();
        for (Client client : clients) {
            if (client.hasIdentifier(leavingClientID)) {
                client.stopAndRemoveAndUnsubscribe();
                return;
            }
        }
    }

    public synchronized boolean hasClients() {
        return !clients.isEmpty();
    }

    public void set(T data) {
        if (hasClients()) {
            javaTwin.publishData(dataId, "" + data);
        }
    }
}
