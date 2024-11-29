package de.ude.ies.elastic_ai.source;

import de.ude.ies.elastic_ai.communicationEndpoints.LocalCommunicationEndpoint;
import de.ude.ies.elastic_ai.communicationEndpoints.RemoteCommunicationEndpoint;
import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.Subscriber;
import java.util.ArrayList;
import java.util.List;

public class AlternativeStartAbleDataSource<T> extends DataSource<T> {

    protected ClientList clients = new ClientList();

    protected class ClientList {

        private class Client implements Subscriber {

            private final String id;
            private boolean isActive;

            private final RemoteCommunicationEndpoint remoteCommunicationEndpoint;

            public Client(String id) {
                this.id = id;
                remoteCommunicationEndpoint = new RemoteCommunicationEndpoint(id);
                remoteCommunicationEndpoint.bindToCommunicationEndpoint(
                    localCommunicationEndpoint.getBroker()
                );
                remoteCommunicationEndpoint.subscribeForStatus(this);
                isActive = true;
            }

            @Override
            public void deliver(Posting posting) {
                if (posting.data().endsWith("0")) {
                    stopAndRemove();
                }
            }

            public void stopAndRemove() {
                if (isActive) {
                    isActive = false;
                    clients.remove(this);
                    remoteCommunicationEndpoint.unsubscribeFromStatus();
                }
            }

            public boolean hasIdentifier(String identifier) {
                return id.equals(identifier);
            }
        }

        private final List<Client> clients = new ArrayList<>();

        public void add(String id) {
            var client = new Client(id);
            clients.add(client);
        }

        public void remove(String id) {
            for (Client client : clients) {
                if (client.hasIdentifier(id)) {
                    client.stopAndRemove();
                    return;
                }
            }
        }

        public boolean hasEntries() {
            return !clients.isEmpty();
        }
    }

    public AlternativeStartAbleDataSource(String dataId) {
        super(dataId);
    }

    @Override
    public void bind(LocalCommunicationEndpoint protocol) {
        this.localCommunicationEndpoint = protocol;
        protocol.subscribeForDataStartRequest(dataId, this::handleNewClient);
        protocol.subscribeForDataStopRequest(dataId, this::handleLeavingClient);
    }

    private void handleNewClient(Posting posting) {
        clients.add(posting.data());
    }

    private void handleLeavingClient(Posting posting) {
        clients.remove(posting.data());
    }

    public boolean hasClients() {
        return clients.hasEntries();
    }
}
