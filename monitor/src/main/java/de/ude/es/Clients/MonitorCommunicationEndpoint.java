package de.ude.es.Clients;

import org.ude.es.communicationEndpoints.LocalCommunicationEndpoint;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;
import org.ude.es.protocol.Posting;
import org.ude.es.protocol.Status;
import org.ude.es.protocol.Subscriber;

public class MonitorCommunicationEndpoint extends LocalCommunicationEndpoint {

    private final ClientList clients;

    public MonitorCommunicationEndpoint(String id) {
        super(id);
        this.clients = new ClientList();
    }

    @Override
    protected void executeOnBind() {
        new StatusMonitor(this, clients, this);
    }

    public ClientList getClientList() {
        return this.clients;
    }

    private static class StatusMonitor implements Subscriber {

        private final LocalCommunicationEndpoint client;
        private final ClientList clients;
        private final MonitorCommunicationEndpoint monitorCommunicationEndpoint;

        public StatusMonitor(
            LocalCommunicationEndpoint client,
            ClientList clientList,
            MonitorCommunicationEndpoint monitorCommunicationEndpoint
        ) {
            this.clients = clientList;
            this.client = client;
            this.monitorCommunicationEndpoint = monitorCommunicationEndpoint;
            createTwinStubAndSubscribeForStatus();
        }

        private void createTwinStubAndSubscribeForStatus() {
            RemoteCommunicationEndpoint stub = new RemoteCommunicationEndpoint(
                "+"
            );
            stub.bindToCommunicationEndpoint(this.client.getBrokerStub());
            stub.subscribeForStatus(this);
        }

        @Override
        public void deliver(Posting posting) {
            String twinID = posting
                .data()
                .substring(
                    posting.data().indexOf(Status.Parameter.ID.getKey()) +
                    Status.Parameter.ID.getKey().length() +
                    1
                );
            twinID = twinID.substring(0, twinID.indexOf(";"));

            boolean twinActive = posting
                .data()
                .contains(Status.State.ONLINE.get());

            System.out.printf(
                "Client with id %s online: %b.%n",
                twinID,
                twinActive
            );

            if (this.client.getDomainAndIdentifier().contains(twinID)) {
                return;
            }

            if (twinActive) {
                int measurementsIndex = posting
                    .data()
                    .indexOf(Status.Parameter.MEASUREMENTS.get());
                if (measurementsIndex >= 0) {
                    String measurements = posting
                        .data()
                        .substring(
                            measurementsIndex +
                            Status.Parameter.MEASUREMENTS.get().length() +
                            1
                        );
                    measurements = measurements.substring(
                        0,
                        measurements.indexOf(";")
                    );

                    clients.addOrUpdateClient(
                        twinID,
                        measurements.split(","),
                        monitorCommunicationEndpoint.getBrokerStub(),
                        monitorCommunicationEndpoint.getDomainAndIdentifier()
                    );
                } else {
                    clients.addOrUpdateClient(
                        twinID,
                        null,
                        monitorCommunicationEndpoint.getBrokerStub(),
                        monitorCommunicationEndpoint.getDomainAndIdentifier()
                    );
                }
            } else {
                ClientData twin = clients.getClient(twinID);
                if (twin != null) {
                    twin.setInactive();
                }
                // if twin == null -> ignore status message,
                //                    no need to add inactive Twin
            }
        }
    }
}
