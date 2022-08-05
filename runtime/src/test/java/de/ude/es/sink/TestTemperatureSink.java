package de.ude.es.sink;

import de.ude.es.comm.*;
import de.ude.es.exampleTwins.ENv5TwinStub;
import de.ude.es.twin.JavaTwin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


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
                    "eip://uni-due.de/es" + id + "/START/temperature",
                    deliveredPosting.topic(),
                    "should have received command to " +
                            "start sending temperature updates");
            assertEquals(
                    "/local",
                    deliveredPosting.data(),
                    "should have received twin identifier " +
                            "to check for its aliveness");
        }

        @Override
        protected void executeOnBind() {
            this.subscribeForDataStartRequest("temperature", this::deliver);
            this.subscribeForDataStopRequest("temperature", this::deliver);
        }

        public void sendUpdate(double data) {
            String topic = "/DATA/temperature";
            Posting response = new Posting(topic, "" + data);
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
                    "eip://uni-due.de/es" + id + "/STOP/temperature",
                    deliveredPosting.topic(),
                    "should have received command to " +
                            "stop sending temperature updates");
            assertEquals(
                    "/local",
                    deliveredPosting.data(),
                    "should have received twin identifier " +
                            "to deregister it as a client");
        }
    }


    private Broker aBroker;
    private ENv5TwinStub device;
    private TwinForDeviceWithTemperatureSensor remoteTwin;
    private TemperatureSink temperature;
    private Posting post;


    @Test
    void temperatureSinkGetsUpdate() {

        aBroker = createBroker();
        device = createDeviceTwin();
        temperature = createTemperatureTwin(device);

        post = createRemoteTemperaturePosting(13.5);
        aBroker.publish(post);

        assertEquals(13.5, temperature.getCurrent());
    }

    @Test
    void multipleTemperatureSinksGetUpdate() {

        aBroker = createBroker();
        device = createDeviceTwin();
        var tempTwin1 = createTemperatureTwin(device);
        var tempTwin2 = createTemperatureTwin(device);

        Posting post = createRemoteTemperaturePosting(13.5);
        aBroker.publish(post);

        assertEquals(13.5, tempTwin1.getCurrent());
        assertEquals(13.5, tempTwin2.getCurrent());
    }

    @Test
    void weDoNotGetUpdateFromWrongDevice() {

        aBroker = createBroker();
        var device1 = createDeviceTwin("/sensor");
        var device2 = createDeviceTwin("/twin4321");
        var tempTwin1 = createTemperatureTwin(device1);
        var tempTwin2 = createTemperatureTwin(device2);

        Posting post = createRemoteTemperaturePosting(13.7);
        aBroker.publish(post);

        assertEquals(13.7, tempTwin1.getCurrent());
        assertEquals(0.0, tempTwin2.getCurrent());
    }

    @Test
    void temperatureSinkCanReceiveMultipleUpdates() {

        createBroker();
        remoteTwin = createRemoteTwin();
        device = createDeviceTwin();
        temperature = createTemperatureTwin(device);

        double[] measuredValues = {13.5, 11.7};
        for (double value : measuredValues) {
            remoteTwin.sendUpdate(value);
            assertEquals(value, temperature.getCurrent());
        }
    }

    @Test
    void temperatureSinkRequestsTemperatureUpdates() {
        createBroker();
        remoteTwin = createRemoteTwin();
        device = createDeviceTwin();
        temperature = createTemperatureTwin(device);

        remoteTwin.registrationReceived();
    }

    @Test
    void temperatureSinkStopsTemperatureUpdates() {
        createBroker();
        remoteTwin = createRemoteTwin();
        device = createDeviceTwin();
        temperature = createTemperatureTwin(device);
        temperature.unbind();

        remoteTwin.deregistrationReceived();
    }

    private Broker createBroker() {
        aBroker = new Broker("eip://uni-due.de/es");
        return aBroker;
    }

    private TemperatureSink createTemperatureTwin(ENv5TwinStub eNv5Twin) {
        temperature = new TemperatureSink("/local");
        temperature.bind(eNv5Twin);
        return temperature;
    }

    private TwinForDeviceWithTemperatureSensor createRemoteTwin() {
        remoteTwin = new TwinForDeviceWithTemperatureSensor("/sensor");
        remoteTwin.bind(aBroker);
        return remoteTwin;
    }

    private ENv5TwinStub createDeviceTwin() {
        return createDeviceTwin("/sensor");
    }

    private ENv5TwinStub createDeviceTwin(String id) {
        device = new ENv5TwinStub(id);
        device.bind(aBroker);
        return device;
    }

    private Posting createRemoteTemperaturePosting(double value) {
        var topic = "/sensor/DATA/temperature";
        post = new Posting(topic, "" + value);
        return post;
    }

}