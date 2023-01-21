package org.ude.es.sink;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.BrokerMock;
import org.ude.es.comm.Posting;
import org.ude.es.comm.PostingType;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

class TestTemperatureSink {

    private static class TwinForDeviceWithTemperatureSensor extends JavaTwin {

        private Posting deliveredPosting = null;

        public TwinForDeviceWithTemperatureSensor(String id) {
            super(id);
        }

        public void registrationReceived() {
            assertNotNull(deliveredPosting, "Should have received a posting");
            assertEquals(
                DOMAIN + this.identifier + PostingType.START.topic(DATA_ID),
                deliveredPosting.topic(),
                "should have received command to start sending temperature updates"
            );
            assertEquals(
                DOMAIN + CONSUMER_ID,
                deliveredPosting.data(),
                "should have received twin identifier to check for its aliveness"
            );
        }

        @Override
        protected void executeOnBind() {
            this.subscribeForDataStartRequest(DATA_ID, this::deliver);
            this.subscribeForDataStopRequest(DATA_ID, this::deliver);
        }

        public void sendUpdate(double data) {
            Posting response = new Posting(
                PostingType.DATA.topic(DATA_ID),
                Double.toString(data)
            );
            this.publish(response, false);
        }

        @Override
        public void publish(Posting posting, boolean retain) {
            endpoint.publish(posting.cloneWithTopicAffix(identifier), false);
        }

        @Override
        public void subscribe(String topic, Subscriber subscriber) {
            endpoint.subscribe(identifier + topic, subscriber);
        }

        @Override
        public String getDomainAndIdentifier() {
            return endpoint.getDomain() + identifier;
        }

        private void deliver(Posting posting) {
            deliveredPosting = posting;
        }

        public void deregistrationReceived() {
            assertNotNull(deliveredPosting, "Should have received a posting");
            assertEquals(
                DOMAIN + this.identifier + PostingType.STOP.topic(DATA_ID),
                deliveredPosting.topic(),
                "should have received command to stop sending temperature updates"
            );
            assertEquals(
                DOMAIN + CONSUMER_ID,
                deliveredPosting.data(),
                "should have received twin identifier to deregister it as a client"
            );
        }
    }

    private static final String SENSOR_ID = "sensor";
    private static final String CONSUMER_ID = "consumer";
    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String DATA_ID = "temp";
    private BrokerMock broker;
    private TwinForDeviceWithTemperatureSensor remote;

    private TwinStub device;

    @BeforeEach
    void setUp() {
        broker = new BrokerMock(DOMAIN);
        device = createDeviceTwin(SENSOR_ID);
        remote = createRemoteTwin();
    }

    @Test
    void temperatureSinkGetsUpdate() {
        var temperatureTwin = createTemperatureSink(device, CONSUMER_ID);

        remote.sendUpdate(13.4);
        assertEquals(13.4, temperatureTwin.getCurrentTemperature());
    }

    @Test
    void multipleTemperatureSinksGetUpdate() {
        var tempTwin1 = createTemperatureSink(device, CONSUMER_ID + "1");
        var tempTwin2 = createTemperatureSink(device, CONSUMER_ID + "2");

        remote.sendUpdate(13.5);
        assertEquals(13.5, tempTwin1.getCurrentTemperature());
        assertEquals(13.5, tempTwin2.getCurrentTemperature());
    }

    @Test
    void weDoNotGetUpdateFromWrongDevice() {
        var tempTwin1 = createTemperatureSink(device, CONSUMER_ID + "1");
        var device2 = createDeviceTwin("twin4321");
        var tempTwin2 = createTemperatureSink(device2, CONSUMER_ID + "2");

        remote.sendUpdate(13.7);
        assertEquals(13.7, tempTwin1.getCurrentTemperature());
        assertEquals(0.0, tempTwin2.getCurrentTemperature());
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

        remote.deregistrationReceived();
    }

    private TwinStub createDeviceTwin(String id) {
        TwinStub device = new TwinStub(id);
        device.bindToCommunicationEndpoint(this.broker);
        return device;
    }

    private TemperatureSink createTemperatureSink(TwinStub device, String id) {
        JavaTwin tempTwin = new JavaTwin(id);
        tempTwin.bindToCommunicationEndpoint(broker);

        TemperatureSink temperature = new TemperatureSink(tempTwin, DATA_ID);
        temperature.connectDataSource(device);
        return temperature;
    }

    private TwinForDeviceWithTemperatureSensor createRemoteTwin() {
        TwinForDeviceWithTemperatureSensor remoteTwin = new TwinForDeviceWithTemperatureSensor(
            SENSOR_ID
        );
        remoteTwin.bindToCommunicationEndpoint(broker);
        return remoteTwin;
    }
}
