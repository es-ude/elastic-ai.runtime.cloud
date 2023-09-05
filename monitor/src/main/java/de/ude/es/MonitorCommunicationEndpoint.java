package de.ude.es;

import org.ude.es.communicationEndpoints.LocalCommunicationEndpoint;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;
import org.ude.es.protocol.BrokerStub;
import org.ude.es.protocol.Posting;
import org.ude.es.protocol.Status;
import org.ude.es.protocol.Subscriber;

public class MonitorCommunicationEndpoint extends LocalCommunicationEndpoint {

    private StatusMonitor statusMonitor;
    private volatile TwinList twins;

    public MonitorCommunicationEndpoint(String id) { //was MonitorTwin
        super(id);
        this.twins = new TwinList();
    }

    @Override
    protected void executeOnBind() {
        statusMonitor = new StatusMonitor(this, twins, this);
    }

    public TwinList getTwinList() {
        return this.twins;
    }

    private static class StatusMonitor implements Subscriber {

        private final LocalCommunicationEndpoint twin;
        private volatile TwinList twins;
        private RemoteCommunicationEndpoint stub;
        private BrokerStub endpoint;
        private MonitorCommunicationEndpoint monitorCommunicationEndpoint;

        public StatusMonitor(
            LocalCommunicationEndpoint twin,
            TwinList twinList,
            MonitorCommunicationEndpoint monitorCommunicationEndpoint
        ) {
            this.twins = twinList;
            this.twin = twin;
            this.monitorCommunicationEndpoint = monitorCommunicationEndpoint;
            createTwinStubAndSubscribeForStatus();
        }

        private void createTwinStubAndSubscribeForStatus() {
            this.stub = new RemoteCommunicationEndpoint("+");
            this.stub.bindToCommunicationEndpoint(this.twin.getBrokerStub());
            this.stub.subscribeForStatus(this);
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

            String twinType = posting
                .data()
                .substring(
                    posting.data().indexOf(Status.Parameter.TYPE.getKey()) +
                    Status.Parameter.TYPE.getKey().length() +
                    1
                );
            twinType = twinType.substring(0, twinType.indexOf(";"));

            boolean twinActive = posting
                .data()
                .contains(Status.State.ONLINE.get());

            System.out.printf(
                "Device of type %s with id %s online: %b.%n",
                twinType,
                twinID,
                twinActive
            );

            if (!twinType.equals(Status.Type.TWIN.get())) {
                // DEVICES not handled by monitor
                return;
            }

            if (this.twin.getDomainAndIdentifier().contains(twinID)) {
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
                    measurements =
                        measurements.substring(0, measurements.indexOf(";"));

                    twins.addOrUpdateTwin(
                        twinID,
                        measurements.split(","),
                        monitorCommunicationEndpoint.getBrokerStub(),
                        monitorCommunicationEndpoint.getDomainAndIdentifier()
                    );
                } else {
                    twins.addOrUpdateTwin(
                        twinID,
                        null,
                        monitorCommunicationEndpoint.getBrokerStub(),
                        monitorCommunicationEndpoint.getDomainAndIdentifier()
                    );
                }
            } else {
                TwinData twin = twins.getTwin(twinID);
                if (twin != null) {
                    twin.setInactive();
                }
                // if twin == null -> ignore status message,
                //                    no need to add inactive Twin
            }
        }
    }
}
