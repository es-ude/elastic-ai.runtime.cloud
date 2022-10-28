package org.ude.es.twinImplementations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.ude.es.comm.Broker;
import org.ude.es.comm.Posting;
import org.ude.es.comm.PostingType;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.JavaTwin;

class TestTemperatureSink {

    private static class TwinForDeviceWithTemperatureSensor extends JavaTwin {

        private Posting deliveredPosting = null;
        private final String id;

        public TwinForDeviceWithTemperatureSensor(String id) {
            super(id);
            this.id = id;
        }

        public void registrationReceived() {
            assertNotNull(deliveredPosting, "Should have received a posting");
            assertEquals(
                DOMAIN + id + "/START/" + DATA_ID,
                deliveredPosting.topic(),
                "should have received command to start sending temperature updates"
            );
            assertEquals(
                SINK_ID,
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
            this.publish(response);
        }

        @Override
        public void publish(Posting posting) {
            endpoint.publish(posting.cloneWithTopicAffix(identifier));
        }

        @Override
        public void subscribe(String topic, Subscriber subscriber) {
            endpoint.subscribe(identifier + topic, subscriber);
        }

        @Override
        public String ID() {
            return endpoint.ID() + identifier;
        }

        private void deliver(Posting posting) {
            deliveredPosting = posting;
        }

        public void deregistrationReceived() {
            assertNotNull(deliveredPosting, "Should have received a posting");
            assertEquals(
                DOMAIN + id + "/STOP/" + DATA_ID,
                deliveredPosting.topic(),
                "should have received command to stop sending temperature updates"
            );
            assertEquals(
                SINK_ID,
                deliveredPosting.data(),
                "should have received twin identifier to deregister it as a client"
            );
        }
    }

    private static final String SENSOR_ID = "/sensor";
    private static final String SINK_ID = SENSOR_ID + "_tempSink";

    private static final String DOMAIN = "eip://uni-due.de/es";

    private static final String DATA_ID = "TEMP";
    private Broker testBroker;
    private TwinForDeviceWithTemperatureSensor remoteTwin;

    @Test
    void temperatureSinkGetsUpdate() {
        createBroker();
        var device = createDeviceTwin();
        var temperatureTwin = createTemperatureTwin(device, SINK_ID);

        testBroker.publish(createRemoteTemperaturePosting(13.5));

        assertEquals(13.5, temperatureTwin.getCurrentTemperature());
    }

    @Test
    void multipleTemperatureSinksGetUpdate() {
        createBroker();
        var device = createDeviceTwin();
        var tempTwin1 = createTemperatureTwin(device, SINK_ID + "1");
        var tempTwin2 = createTemperatureTwin(device, SINK_ID + "2");

        testBroker.publish(createRemoteTemperaturePosting(13.5));

        assertEquals(13.5, tempTwin1.getCurrentTemperature());
        assertEquals(13.5, tempTwin2.getCurrentTemperature());
    }

    @Test
    void weDoNotGetUpdateFromWrongDevice() {
        createBroker();
        var device1 = createDeviceTwin("/sensor");
        var tempTwin1 = createTemperatureTwin(device1, SINK_ID + "1");
        var device2 = createDeviceTwin("/twin4321");
        var tempTwin2 = createTemperatureTwin(device2, SINK_ID + "2");

        testBroker.publish(createRemoteTemperaturePosting(13.7));

        assertEquals(13.7, tempTwin1.getCurrentTemperature());
        assertEquals(0.0, tempTwin2.getCurrentTemperature());
    }

    @Test
    void temperatureSinkCanReceiveMultipleUpdates() {
        createBroker();
        createRemoteTwin();
        var device = createDeviceTwin();
        var temperature = createTemperatureTwin(device, SINK_ID);

        double[] measuredValues = { 13.5, 11.7 };
        for (double value : measuredValues) {
            this.remoteTwin.sendUpdate(value);
            assertEquals(value, temperature.getCurrentTemperature());
        }
    }

    @Test
    void temperatureSinkRequestsTemperatureUpdates() {
        createBroker();
        createRemoteTwin();
        var device = createDeviceTwin();
        createTemperatureTwin(device, SINK_ID);

        remoteTwin.registrationReceived();
    }

    @Test
    void temperatureSinkStopsTemperatureUpdates() {
        createBroker();
        createRemoteTwin();
        var device = createDeviceTwin();
        var temperature = createTemperatureTwin(device, SINK_ID);
        temperature.disconnectDataSource();

        remoteTwin.deregistrationReceived();
    }

    private void createBroker() {
        this.testBroker = new Broker(DOMAIN);
    }

    private TemperatureSink createTemperatureTwin(
        ENv5TwinStub device,
        String sinkId
    ) {
        TemperatureSink temperature = new TemperatureSink(sinkId, DATA_ID);
        temperature.bind(testBroker);
        temperature.connectDataSource(device);
        return temperature;
    }

    private void createRemoteTwin() {
        this.remoteTwin = new TwinForDeviceWithTemperatureSensor(SENSOR_ID);
        this.remoteTwin.bind(testBroker);
    }

    private ENv5TwinStub createDeviceTwin() {
        return createDeviceTwin(SENSOR_ID);
    }

    private ENv5TwinStub createDeviceTwin(String id) {
        ENv5TwinStub device = new ENv5TwinStub(id);
        device.bind(this.testBroker);
        return device;
    }

    private Posting createRemoteTemperaturePosting(double value) {
        return new Posting(
            PostingType.DATA.topic(DATA_ID),
            Double.toString(value)
        )
            .cloneWithTopicAffix(SENSOR_ID);
    }
}
