package de.ude.ies.elastic_ai.source;

import de.ude.ies.elastic_ai.communicationEndpoints.LocalCommunicationEndpoint;
import de.ude.ies.elastic_ai.communicationEndpoints.RemoteCommunicationEndpoint;
import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.Subscriber;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple template class for data sources. Can be used by twins that measure
 * some data to make it available to clients. This class handles listening for
 * START and STOP messages and will subscribe for the status of all its
 * clients to detect if none of them is available anymore and stop sending
 * updates in that case.
 *
 * @param <T> the type of the measured data
 */
public class ControllableDataSource<T> {

    protected final List<Client> clients = new ArrayList<>();
    protected LocalCommunicationEndpoint localCommunicationEndpoint;
    protected final String dataId;

    protected class Client implements Subscriber {

        private final String clientIdentifier;
        private boolean isActive = true;
        private final RemoteCommunicationEndpoint remoteCommunicationEndpoint;

        public Client(String clientIdentifier) {
            this.clientIdentifier = clientIdentifier;

            remoteCommunicationEndpoint = new RemoteCommunicationEndpoint(clientIdentifier);
            remoteCommunicationEndpoint.bindToCommunicationEndpoint(
                localCommunicationEndpoint.getBroker()
            );
            remoteCommunicationEndpoint.subscribeForStatus(this);
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
            handleLwtReceived();
        }

        private void deactivate() {
            isActive = false;
            clients.remove(this);
            remoteCommunicationEndpoint.unsubscribeFromStatus();
        }

        public boolean hasIdentifier(String identifier) {
            return clientIdentifier.equals(identifier);
        }
    }

    public ControllableDataSource(LocalCommunicationEndpoint twin, String dataId) {
        this.dataId = dataId;
        this.localCommunicationEndpoint = twin;

        localCommunicationEndpoint.subscribeForDataStartRequest(this.dataId, this::handleNewClient);
        localCommunicationEndpoint.subscribeForDataStopRequest(
            this.dataId,
            this::handleLeavingClient
        );
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

    public synchronized int getNumberOfClients() {
        return clients.size();
    }

    public void set(T data) {
        if (hasClients()) {
            localCommunicationEndpoint.publishData(dataId, data.toString());
        }
    }
}
