package de.ude.es;

import de.ude.es.comm.*;
import de.ude.es.sink.TemperatureSink;
import de.ude.es.source.TemperatureSource;
import de.ude.es.twin.DigitalTwin;
import de.ude.es.twin.TwinWithHeartbeat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

//@Testcontainers
public class IntegrationTest4ExternalBroker {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String PRODUCER = "/producer";
    private static final String CONSUMER = "/consumer";
    private static final int HEARTBEAT_INTERVAL = 1000; //in ms

//    @Container
//    public GenericContainer brokerCont = new GenericContainer(DockerImageName.parse("eclipse-mosquitto:1.6.14"))
//            .withExposedPorts(1883);

    private static class TwinThatOffersTemperature {

        private TwinWithHeartbeat localDeviceTwin;
        private TemperatureSource temperatureSource;

        public TwinThatOffersTemperature(HivemqBroker broker, String id) {
            createTwinForLocalDevice(broker, id);
            createTemperatureSource();
        }

        private void createTwinForLocalDevice(HivemqBroker broker, String id) {
            localDeviceTwin = new TwinWithHeartbeat(id);
            localDeviceTwin.bind(broker);
            localDeviceTwin.startHeartbeats(new TimerMock(), HEARTBEAT_INTERVAL);
        }

        private void createTemperatureSource() {
            temperatureSource = new TemperatureSource(new TimerMock());
            temperatureSource.bind(localDeviceTwin);
        }

        public void setNewTemperatureMeasured(double temperature) {
            System.out.println("set temperature");
            temperatureSource.set(temperature);
        }

        public boolean hasClients() {
            return temperatureSource.hasClients();
        }

    }

    private static class TwinThatConsumesTemperature {

        private DigitalTwin remoteDeviceTwin;
        private TemperatureSink temperatureSink;

        public TwinThatConsumesTemperature(HivemqBroker broker, String local, String remote) {
            createTwinForLocalDevice(broker, local);
            createTwinForRemoteDevice(broker, remote);
            createTemperatureSink();
        }

        private void createTwinForLocalDevice(HivemqBroker broker, String id) {
            var localDeviceTwin = new TwinWithHeartbeat(id);
            localDeviceTwin.bind(broker);
            localDeviceTwin.startHeartbeats(new TimerMock(), HEARTBEAT_INTERVAL);
        }

        private void createTwinForRemoteDevice(HivemqBroker broker, String id) {
            remoteDeviceTwin = new DigitalTwin(id);
            remoteDeviceTwin.bind(broker);
        }

        private void createTemperatureSink() {
            temperatureSink = new TemperatureSink(remoteDeviceTwin.ID());
            temperatureSink.bind(remoteDeviceTwin);
        }

        public void checkTemperatureIs(double expected) {
            assertEquals(expected, temperatureSink.getCurrent());
        }

        public boolean isNewTemperatureAvailable() {
            return temperatureSink.isNewTemperatureAvailable();
        }

    }

    public static class HeartbeatSubscriber {

        private static class DataSubscriber implements Subscriber {

            public static int heartbeatCount = 0;
            public static String lastData = "";

            @Override
            public void deliver(Posting posting) {
                lastData = posting.data();
                heartbeatCount++;
            }
        }

        private Protocol protocol;
        private DataSubscriber subscriber;

        public void bind(CommunicationEndpoint endpoint) {
            bind(new Protocol(endpoint));
        }

        public void bind(Protocol protocol) {
            this.protocol = protocol;
            this.subscriber = new DataSubscriber();
            this.protocol.subscribeForHeartbeat(DOMAIN + "/+", subscriber);
        }

        public int getHeartbeatCount() {
            return DataSubscriber.heartbeatCount;
        }

        public String getLastData() {
            return DataSubscriber.lastData;
        }

        public void reset() {
            DataSubscriber.heartbeatCount = 0;
            DataSubscriber.lastData = "";
        }
    }


    private HivemqBroker broker;
    private DigitalTwin it;
    private TimerMock timer;

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
        broker = new HivemqBroker(DOMAIN);
        var sensingDevice = new TwinThatOffersTemperature(
                broker, PRODUCER);

        var consumingDevice = new TwinThatConsumesTemperature(
                broker, CONSUMER, PRODUCER);

        while (!sensingDevice.hasClients()) ;
        sensingDevice.setNewTemperatureMeasured(11.6);
        while (!consumingDevice.isNewTemperatureAvailable()) ;
        consumingDevice.checkTemperatureIs(11.6);
        sensingDevice.setNewTemperatureMeasured(1.7);
        while (!consumingDevice.isNewTemperatureAvailable()) ;
        consumingDevice.checkTemperatureIs(1.7);

        broker.closeConnection();
    }


    @Test
    void communicationCanBeStopped() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN);

        TemperatureSource source = createTemperatureSource();
        TemperatureSink sink = createTemperatureSink(CONSUMER);
        while (!source.hasClients()) ;
        sink.unbind();
        while (source.hasClients()) ;
        source.set(9.9);
        Thread.sleep(1000);
        assertEquals(0.0, sink.getCurrent());
        broker.closeConnection();
    }

    @Test
    void communicationCanBeResumed() {
        broker = new HivemqBroker(DOMAIN);

        TemperatureSource source = createTemperatureSource();
        TemperatureSink sink = createTemperatureSink(CONSUMER);

        while (!source.hasClients()) ;
        sink.unbind();

        while (source.hasClients()) ;
        assertFalse(source.hasClients());

        sink.bind(it);
        while (!source.hasClients()) ;
        assertTrue(source.hasClients());

        source.set(11.4);
        while (!sink.isNewTemperatureAvailable()) ;
        assertEquals(11.4, sink.getCurrent());

        broker.closeConnection();
    }

    @Test
    void sourceAndTwoSinksCanCommunicate() {
        broker = new HivemqBroker(DOMAIN);

        TemperatureSource temperatureSource = createTemperatureSource();
        TemperatureSink sink1 = createTemperatureSink(CONSUMER + "1");
        TemperatureSink sink2 = createTemperatureSink(CONSUMER + "2");

        while (!temperatureSource.hasClients()) ;
        temperatureSource.set(11.4);
        while (!sink1.isNewTemperatureAvailable() || !sink2.isNewTemperatureAvailable()) ;

        assertEquals(11.4, sink1.getCurrent());
        assertEquals(11.4, sink2.getCurrent());

        broker.closeConnection();
    }

    @Test
    void heartbeatIsSendBySourceByStart() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN);

        Runnable myRunnable = this::createTemperatureSource;
        HeartbeatSubscriber heartbeatSubscriber = createHeartbeatSubscriber();

        myRunnable.run();

        Thread.sleep(100);
        assertEquals(1, heartbeatSubscriber.getHeartbeatCount());

        heartbeatSubscriber.reset();
        broker.closeConnection();
    }

    @Test
    void heartbeatsAreSendBySource() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN);

        Runnable myRunnable = this::createTemperatureSource;
        HeartbeatSubscriber heartbeatSubscriber = createHeartbeatSubscriber();

        myRunnable.run();

        timer.fire();
        Thread.sleep(100);
        assertEquals(2, heartbeatSubscriber.getHeartbeatCount());

        timer.fire();
        Thread.sleep(100);
        assertEquals(3, heartbeatSubscriber.getHeartbeatCount());

        heartbeatSubscriber.reset();
        broker.closeConnection();
    }

    @Test
    void heartbeatIncludesSender() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN);

        Runnable myRunnable = this::createTemperatureSource;
        HeartbeatSubscriber heartbeatSubscriber = createHeartbeatSubscriber();

        myRunnable.run();

        Thread.sleep(1000);
        assertEquals("/producer", heartbeatSubscriber.getLastData());

        timer.fire();
        Thread.sleep(1000);
        assertEquals("/producer", heartbeatSubscriber.getLastData());

        heartbeatSubscriber.reset();
        broker.closeConnection();
    }

    private HeartbeatSubscriber createHeartbeatSubscriber() {
        var sink = new TwinWithHeartbeat(PRODUCER);
        sink.bind(broker);

        var heartbeatSubscriber = new HeartbeatSubscriber();
        heartbeatSubscriber.bind(sink);

        return heartbeatSubscriber;
    }

    private TemperatureSource createTemperatureSource() {
        timer = new TimerMock();

        var source = new TwinWithHeartbeat(PRODUCER);
        source.bind(broker);
        source.startHeartbeats(timer, HEARTBEAT_INTERVAL);

        var temperatureSource = new TemperatureSource(timer);
        temperatureSource.bind(source);

        return temperatureSource;
    }

    private TemperatureSink createTemperatureSink(String local) {
        var sink = new TwinWithHeartbeat(local);
        sink.bind(broker);
        sink.startHeartbeats(new TimerMock(), HEARTBEAT_INTERVAL);

        it = new DigitalTwin(PRODUCER);
        it.bind(broker);
        var tempSink = new TemperatureSink(sink.ID());
        tempSink.bind(it);

        return tempSink;
    }
}
