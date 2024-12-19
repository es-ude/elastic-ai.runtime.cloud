package de.ude.ies.elastic_ai.sink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.ude.ies.elastic_ai.comm.BrokerMock;
import de.ude.ies.elastic_ai.communicationEndpoints.LocalCommunicationEndpoint;
import de.ude.ies.elastic_ai.communicationEndpoints.RemoteCommunicationEndpoint;
import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.PostingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestTemperatureSink {

    private static class deviceWithTemperatureSensor extends LocalCommunicationEndpoint {

        private Posting deliveredPosting = null;

        public deviceWithTemperatureSensor(String id) {
            super(id, "localCE");
        }

        public void registrationReceived() {
            assertNotNull(deliveredPosting, "Should have received a posting");
            assertEquals(
                DOMAIN + "/" + this.identifier + PostingType.START.topic(DATA_ID),
                deliveredPosting.topic(),
                "should have received command to start sending temperature updates"
            );
            assertEquals(
                CONSUMER_ID,
                deliveredPosting.data(),
                "should have received device identifier to check for its aliveness"
            );
        }

        @Override
        protected void executeOnBind() {
            this.subscribeForDataStartRequest(DATA_ID, this::deliver);
            this.subscribeForDataStopRequest(DATA_ID, this::deliver);
        }

        public void sendUpdate(double data) {
            Posting response = new Posting(PostingType.DATA.topic(DATA_ID), Double.toString(data));
            this.publish(response, false);
        }

        @Override
        public String getDomainAndIdentifier() {
            return broker.getDomain() + identifier;
        }

        private void deliver(Posting posting) {
            deliveredPosting = posting;
        }

        public void deRegistrationReceived() {
            assertNotNull(deliveredPosting, "Should have received a posting");
            assertEquals(
                DOMAIN + "/" + this.identifier + PostingType.STOP.topic(DATA_ID),
                deliveredPosting.topic(),
                "should have received command to stop sending temperature updates"
            );
            assertEquals(
                CONSUMER_ID,
                deliveredPosting.data(),
                "should have received device identifier to deregister it as a client"
            );
        }
    }

    private static final String SENSOR_ID = "sensor";
    private static final String CONSUMER_ID = "consumer";
    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String DATA_ID = "temp";
    private BrokerMock broker;
    private deviceWithTemperatureSensor remote;

    private RemoteCommunicationEndpoint device;

    @BeforeEach
    void setUp() {
        broker = new BrokerMock(DOMAIN);
        device = createDevice(SENSOR_ID);
        remote = createRemoteDevice();
    }

    @Test
    void temperatureSinkGetsUpdate() {
        var temperatureDevice = createTemperatureSink(device, CONSUMER_ID);

        remote.sendUpdate(13.4);
        assertEquals(13.4, temperatureDevice.getCurrentTemperature());
    }

    @Test
    void weDoNotGetUpdateFromWrongDevice() {
        var tempDevice1 = createTemperatureSink(device, CONSUMER_ID + "1");
        var device2 = createDevice("Device4321");
        var tempDevice2 = createTemperatureSink(device2, CONSUMER_ID + "2");

        remote.sendUpdate(13.7);
        assertEquals(13.7, tempDevice1.getCurrentTemperature());
        assertEquals(0.0, tempDevice2.getCurrentTemperature());
    }

    @Test
    void temperatureSinkCanReceiveMultipleUpdates() {
        var temperature = createTemperatureSink(device, CONSUMER_ID);

        double[] measuredValues = { 13.5, 11.7 };
        for (double value : measuredValues) {
            remote.sendUpdate(value);
            assertEquals(value, temperature.getCurrentTemperature());
        }
    }

    @Test
    void temperatureSinkRequestsTemperatureUpdates() {
        createTemperatureSink(device, CONSUMER_ID);

        remote.registrationReceived();
    }

    @Test
    void temperatureSinkStopsTemperatureUpdates() {
        var temperature = createTemperatureSink(device, CONSUMER_ID);
        temperature.disconnectDataSource();

        remote.deRegistrationReceived();
    }

    private RemoteCommunicationEndpoint createDevice(String id) {
        RemoteCommunicationEndpoint device = new RemoteCommunicationEndpoint(id);
        device.bindToCommunicationEndpoint(this.broker);
        return device;
    }

    private TemperatureSink createTemperatureSink(RemoteCommunicationEndpoint device, String id) {
        TemperatureSink temperature = new TemperatureSink(id, DATA_ID);
        temperature.connectDataSource(device);
        return temperature;
    }

    private deviceWithTemperatureSensor createRemoteDevice() {
        deviceWithTemperatureSensor remoteDevice = new deviceWithTemperatureSensor(SENSOR_ID);
        remoteDevice.bindToCommunicationEndpoint(broker);
        return remoteDevice;
    }
}
