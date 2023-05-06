package de.ude.es;

import org.ude.es.comm.Posting;
import org.ude.es.comm.Status;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

public class MonitorTwin extends JavaTwin {

    private static class StatusMonitor implements Subscriber {

        private volatile TwinList twins;
        private final JavaTwin twin;
        private TwinStub stub;

        public StatusMonitor(JavaTwin twin, TwinList twinList) {
            this.twins = twinList;
            this.twin = twin;
            createTwinStubAndSubscribeForStatus();
        }

        private void createTwinStubAndSubscribeForStatus() {
            this.stub = new TwinStub("+");
            this.stub.bindToCommunicationEndpoint(this.twin.getEndpoint());
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

                    twins.addOrUpdateTwin(twinID, measurements.split(","));
                } else {
                    twins.addOrUpdateTwin(twinID, null);
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

    private StatusMonitor monitor;
    private volatile TwinList twins;

    public MonitorTwin(String id) {
        super(id);
        this.twins = new TwinList();
    }

    @Override
    protected void executeOnBind() {
        monitor = new StatusMonitor(this, twins);
    }

    public TwinList getTwinList() {
        return this.twins;
    }
}
