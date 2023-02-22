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
            String[] twinData = posting.data().split(";");
            String twinID = posting
                .data()
                .substring(0, posting.data().indexOf(";"));
            boolean twinActive = posting.data().endsWith("1");

            if (!"TWIN".equals(twinData[1].trim())) {
                System.out.printf(
                    "Device of type %s with id %s detected.%n",
                    twinData[1],
                    twinData[0]
                );
                // DEVICES not handled by monitor
                return;
            }

            if (
                this.twin.getDomainAndIdentifier().contains(twinData[0].trim())
            ) {
                return;
            }

            if (twinActive) {
                twins.addTwin(twinID);
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
