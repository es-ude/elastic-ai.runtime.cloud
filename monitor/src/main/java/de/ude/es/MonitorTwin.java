package de.ude.es;

import org.ude.es.comm.Posting;
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
            String twinID = posting.data().substring(posting.data().indexOf("ID:") + 3);
            twinID = twinID.substring(0, twinID.indexOf(";"));

            String twinType = posting.data().substring(posting.data().indexOf("TYPE:") + 5);
            twinType = twinType.substring(0, twinType.indexOf(";"));

            boolean twinActive = posting.data().contains("STATE:ONLINE");

            System.out.printf(
                    "Device of type %s with id %s online: %b.%n",
                    twinType,
                    twinID,
                    twinActive
            );

            if (!twinType.equals("TWIN")) {
                // DEVICES not handled by monitor
                return;
            }

            if (this.twin.getDomainAndIdentifier().contains(twinID)) {
                return;
            }

            if (twinActive) {
                String measurements = posting.data().substring(posting.data().indexOf("MEASUREMENTS:") + 13);
                measurements = measurements.substring(0, measurements.indexOf(";"));

                twins.addOrUpdateTwin(twinID, measurements.split(","));
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
