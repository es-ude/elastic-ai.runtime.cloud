package de.ude.es;

import de.ude.es.comm.Broker;
import de.ude.es.exampleTwins.TwinWithHeartbeat;
import de.ude.es.sink.TemperatureSink;
import de.ude.es.source.TemperatureSource;
import de.ude.es.twin.TwinStub;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This is an integration test class that is also meant to provide
 * an example on how to use the provided API to implement twins
 * that measure temperature and twin that access temperature as data.
 */
public class IntegrationTest4TwinInteraction {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String PRODUCER = "/producer";
    private static final String CONSUMER = "/consumer";
    private static final int HEARTBEAT_INTERVAL = 1000; //in ms


    private static class TwinThatOffersTemperature {

        private TwinWithHeartbeat localDeviceTwin;
        private TemperatureSource temperatureSource;

        public TwinThatOffersTemperature(Broker broker, String id) {
            createTwinForLocalDevice(broker, id);
            createTemperatureSource();
        }

        private void createTwinForLocalDevice(Broker broker, String id) {
            localDeviceTwin = new TwinWithHeartbeat(id);
            localDeviceTwin.bind(broker);
            localDeviceTwin.startHeartbeats(new TimerMock(), HEARTBEAT_INTERVAL);
        }

        private void createTemperatureSource() {
            temperatureSource = new TemperatureSource(new TimerMock());
            temperatureSource.bind(localDeviceTwin);
        }

        public void setNewTemperatureMeasured(double temperature) {
            temperatureSource.set(temperature);
        }

    }

    private static class TwinThatConsumesTemperature {

        private TwinStub remoteDeviceTwin;
        private TemperatureSink temperatureSink;

        public TwinThatConsumesTemperature(Broker broker, String local, String remote) {
            createTwinForLocalDevice(broker, local);
            createTwinForRemoteDevice(broker, remote);
            createTemperatureSink();
        }

        private void createTwinForLocalDevice(Broker broker, String id) {
            var localDeviceTwin = new TwinWithHeartbeat(id);
            localDeviceTwin.bind(broker);
            localDeviceTwin.startHeartbeats(new TimerMock(), HEARTBEAT_INTERVAL);
        }

        private void createTwinForRemoteDevice(Broker broker, String id) {
            remoteDeviceTwin = new TwinStub(id);
            remoteDeviceTwin.bind(broker);
        }

        private void createTemperatureSink() {
            temperatureSink = new TemperatureSink(remoteDeviceTwin.ID());
            temperatureSink.bind(remoteDeviceTwin);
        }

        public void checkTemperatureIs(double expected) {
            assertEquals(expected, temperatureSink.getCurrent());
        }

    }

    private Broker broker;
    private TwinStub it;

    /**
     * This is an integration test that shows the basic interaction
     * between twins by exchanging a temperature that is measured by
     * one device ("producer") with another device ("consumer") that
     * wants to access it.
     * The example shows how to achieve this with
     * - a TemperatureSource on the producer side
     * - and a TemperatureSink on the consumer side.
     */
    @Test
    void twinsCanCommunicate() {
        var broker = new Broker(DOMAIN);

        var sensingDevice = new TwinThatOffersTemperature(broker, PRODUCER);

        var consumingDevice = new TwinThatConsumesTemperature(broker, CONSUMER, PRODUCER);

        sensingDevice.setNewTemperatureMeasured(11.6);
        consumingDevice.checkTemperatureIs(11.6);
        // ...
        sensingDevice.setNewTemperatureMeasured(1.7);
        consumingDevice.checkTemperatureIs(1.7);
    }

    @Test
    void communicationCanBeStopped() {
        broker = new Broker(DOMAIN);

        TemperatureSource source = createTemperatureSource();
        TemperatureSink sink = createTemperatureSink(CONSUMER);

        sink.unbind();
        source.set(9.9);
        assertEquals(0.0, sink.getCurrent());
    }

    @Test
    void communicationCanBeResumed() {
        broker = new Broker(DOMAIN);

        TemperatureSource source = createTemperatureSource();
        TemperatureSink sink = createTemperatureSink(CONSUMER);

        sink.unbind();
        sink.bind(it);

        source.set(11.4);
        assertEquals(11.4, sink.getCurrent());
    }

    @Test
    void sourceAndTwoSinksCanCommunicate() {
        broker = new Broker(DOMAIN);

        TemperatureSource temperatureSource = createTemperatureSource();
        TemperatureSink sink1 = createTemperatureSink(CONSUMER + "1");
        TemperatureSink sink2 = createTemperatureSink(CONSUMER + "2");

        temperatureSource.set(11.4);

        assertEquals(11.4, sink1.getCurrent());
        assertEquals(11.4, sink2.getCurrent());
    }

    private TemperatureSource createTemperatureSource() {
        var source = new TwinWithHeartbeat(PRODUCER);
        source.bind(broker);
        source.startHeartbeats(new TimerMock(), HEARTBEAT_INTERVAL);

        var temperatureSource = new TemperatureSource(new TimerMock());
        temperatureSource.bind(source);

        return temperatureSource;
    }

    private TemperatureSink createTemperatureSink(String local) {
        var sink = new TwinWithHeartbeat(local);
        sink.bind(broker);
        sink.startHeartbeats(new TimerMock(), HEARTBEAT_INTERVAL);

        it = new TwinStub(PRODUCER);
        it.bind(broker);
        var tempSink = new TemperatureSink(sink.ID());
        tempSink.bind(it);

        return tempSink;
    }

}
