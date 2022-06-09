package de.ude.es.source;

import de.ude.es.comm.CommunicationEndpoint;
import de.ude.es.comm.Posting;
import de.ude.es.comm.Protocol;
import de.ude.es.comm.Subscriber;
import de.ude.es.util.Timeout;
import de.ude.es.util.Timer;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple template class for data sources. Can be used by
 * twins that measure some data to make it available to
 * clients. This class handles listening for START and STOP
 * messages and will subscribe for the heartbeats of all its
 * clients to detect if none of them is available anymore and
 * stop sending updates in that case.
 *
 * @param <T> the type of the measured data
 */
public class ControllableDataSource<T> {

    protected final List<Client> clients = new ArrayList<>();
    protected final Timer timer;
    protected Protocol protocol;

    protected final String dataId;
    protected static final int TIMEOUT_LENGTH = 1000;

    protected class Client implements Subscriber {

        private final String heartbeatSource;
        private boolean isActive = true;
        private final Timeout timeout;

        public Client(String heartbeatSource) {
            this.heartbeatSource = heartbeatSource;
            protocol.subscribeForHeartbeat(heartbeatSource, this);
            timeout = timer.register(TIMEOUT_LENGTH, this::handleNoHeartbeatReceived);
        }

        @Override
        public void deliver(Posting posting) {
            handleHeartbeatReceived();
        }

        private void handleHeartbeatReceived() {
            if (isActive)
                timeout.restart();
        }

        private void handleNoHeartbeatReceived(Timeout timeout) {
            if (isActive)
                deactivate();
        }

        public void stopAndRemoveAndUnsubscribe() {
            if (isActive)
                deactivate();
        }

        private void deactivate() {
            isActive = false;
            clients.remove(this);
            protocol.unsubscribeFromHeartbeat(heartbeatSource, this);
        }

        public boolean hasIdentifier(String identifier) {
            return heartbeatSource.equals(identifier);
        }
    }


    public ControllableDataSource(String dataId, Timer timer) {
        this.timer = timer;
        this.dataId = dataId;
    }

    public void bind(Protocol protocol) {
        this.protocol = protocol;
        protocol.subscribeForDataStartRequest(dataId, this::handleNewClient);
        protocol.subscribeForDataStopRequest(dataId, this::handleLeavingClient);
    }

    public void bind(CommunicationEndpoint endpoint) {
        bind(new Protocol(endpoint));
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
        if (hasClients())
            protocol.publishData(dataId, "" + data);
    }

}
