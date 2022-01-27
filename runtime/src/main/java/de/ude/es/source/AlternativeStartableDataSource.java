package de.ude.es.source;

import de.ude.es.comm.Posting;
import de.ude.es.comm.Protocol;
import de.ude.es.comm.Subscriber;

import java.util.ArrayList;
import java.util.List;


public class AlternativeStartableDataSource<T> extends DataSource<T> {

    protected ClientList clients = new ClientList();

    protected class ClientList {

        private class Client implements Subscriber {

            private final String id;
            private boolean isActive;

            public Client(String id) {
                this.id = id;
                protocol.subscribeForLost(id, this);
                isActive = true;
            }

            @Override
            public void deliver(Posting posting) {
                stopAndRemove();
            }

            public void stopAndRemove() {
                if(isActive) {
                    isActive = false;
                    clients.remove(this);
                    protocol.unsubscribeFromLost(id, this);
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
            for(Client client : clients) {
                if(client.hasIdentifier(id)) {
                    client.stopAndRemove();
                    return;
                }
            }
        }

        public boolean hasEntries() {
            return !clients.isEmpty();
        }

    }


    public AlternativeStartableDataSource(String dataId) {
        super(dataId);
    }

    @Override
    public void bind(Protocol protocol) {
        this.protocol = protocol;
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
