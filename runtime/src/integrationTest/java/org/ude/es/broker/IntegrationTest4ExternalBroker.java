package org.ude.es.broker;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.ude.es.comm.CommunicationEndpoint;
import org.ude.es.comm.HivemqBroker;
import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;
import org.ude.es.source.TemperatureSource;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;
import org.ude.es.twinImplementations.TemperatureSink;

@Testcontainers
public class IntegrationTest4ExternalBroker {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private int PORT;
    private static final String PRODUCER = "/producer";
    private static final String CONSUMER = "/consumer";

    @Container
    public GenericContainer<?> brokerCont = new GenericContainer<>(
        DockerImageName.parse("eclipse-mosquitto:1.6.14")
    )
        .withExposedPorts(1883);

    @BeforeEach
    void setUp() {
        PORT = brokerCont.getFirstMappedPort();
    }

    private static class TwinThatOffersTemperature {

        private JavaTwin localDeviceTwin;
        private TemperatureSource temperatureSource;

        public TwinThatOffersTemperature(HivemqBroker broker, String id) {
            createTwinForLocalDevice(broker, id);
            createTemperatureSource();
        }

        private void createTwinForLocalDevice(HivemqBroker broker, String id) {
            localDeviceTwin = new JavaTwin(id);
            localDeviceTwin.bind(broker);
        }

        private void createTemperatureSource() {
            temperatureSource = new TemperatureSource();
            temperatureSource.bind(localDeviceTwin);
        }

        public void setNewTemperatureMeasured(double temperature) {
            temperatureSource.set(temperature);
        }

        public boolean hasClients() {
            return temperatureSource.hasClients();
        }
    }

    private static class TwinThatConsumesTemperature {

        TwinStub remoteDeviceTwin;
        TemperatureSink temperatureSink;

        public TwinThatConsumesTemperature(
            HivemqBroker broker,
            String local,
            String remote
        ) {
            createTwinForLocalDevice(broker, local);
            createTwinForRemoteDevice(broker, remote);
            createTemperatureSink(broker);
        }

        private void createTwinForLocalDevice(HivemqBroker broker, String id) {
            JavaTwin localDeviceTwin = new JavaTwin(id);
            localDeviceTwin.bind(broker);
        }

        private void createTwinForRemoteDevice(HivemqBroker broker, String id) {
            remoteDeviceTwin = new TwinStub(id);
            remoteDeviceTwin.bind(broker);
        }

        private void createTemperatureSink(HivemqBroker broker) {
            temperatureSink =
                new TemperatureSink(remoteDeviceTwin.ID() + "_sink");
            temperatureSink.bind(broker);
            temperatureSink.connectDataSource(remoteDeviceTwin);
        }

        public void checkTemperatureIs(double expected) {
            assertEquals(
                expected,
                this.temperatureSink.getCurrentTemperature()
            );
        }

        public boolean isNewTemperatureAvailable() {
            return this.temperatureSink.isNewTemperatureAvailable();
        }
    }

    private static class TwinForTestStatusMessage {

        private JavaTwin testTwin;

        public TwinForTestStatusMessage(HivemqBroker broker, String id) {
            testTwin = new JavaTwin(id);
            testTwin.bind(broker);
        }

        public void publishStatusOnline() throws InterruptedException {
            testTwin.publishStatus(true);
        }

        public void publishStatusOffline() throws InterruptedException {
            testTwin.publishStatus(false);
        }
    }

    public static class StatusSubscriber {

        private static class DataSubscriber implements Subscriber {

            public static Posting lastPosting = null;

            @Override
            public void deliver(Posting posting) {
                lastPosting = posting;
            }
        }

        private TwinStub twinStub;
        private DataSubscriber subscriber;

        public void bind(CommunicationEndpoint broker) {
            this.twinStub = new TwinStub("+");
            twinStub.bind(broker);
            this.subscriber = new DataSubscriber();
            this.twinStub.subscribeForStatus(subscriber);
        }

        public String getLastData() {
            return DataSubscriber.lastPosting.data();
        }

        public void reset() {
            DataSubscriber.lastPosting = null;
        }
    }

    private HivemqBroker broker;
    private TwinStub stub;

    /**
     * This is an integration test that shows the basic interaction between
     * twins by exchanging a temperature that is measured by one device
     * ("producer") with another device ("consumer") that wants to access it.
     * The example shows how to achieve this with - a TemperatureSource on the
     * producer side - and a TemperatureSink on the consumer side.
     */

    @Test
    void twinsCanCommunicate() {
        broker = new HivemqBroker(DOMAIN, IP, PORT);
        var sensingDevice = new TwinThatOffersTemperature(broker, PRODUCER);

        var consumingDevice = new TwinThatConsumesTemperature(
            broker,
            CONSUMER,
            PRODUCER
        );

        while (!sensingDevice.hasClients());
        sensingDevice.setNewTemperatureMeasured(11.6);
        while (!consumingDevice.isNewTemperatureAvailable());
        consumingDevice.checkTemperatureIs(11.6);
        sensingDevice.setNewTemperatureMeasured(1.7);
        while (!consumingDevice.isNewTemperatureAvailable());
        consumingDevice.checkTemperatureIs(1.7);

        broker.closeConnection();
    }

    @Test
    void communicationCanBeStopped() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN, IP, PORT);

        TemperatureSource source = createTemperatureSource();
        TemperatureSink sink = createTemperatureSink(CONSUMER);

        while (!source.hasClients());
        sink.disconnectDataSource();
        while (source.hasClients());
        source.set(9.9);
        Thread.sleep(1000);
        assertEquals(0.0, sink.getCurrentTemperature());
        broker.closeConnection();
    }

    @Test
    void communicationCanBeResumed() {
        broker = new HivemqBroker(DOMAIN, IP, PORT);

        TemperatureSource source = createTemperatureSource();
        TemperatureSink sink = createTemperatureSink(CONSUMER);

        while (!source.hasClients());
        sink.disconnectDataSource();

        while (source.hasClients());
        assertFalse(source.hasClients());

        sink.connectDataSource(stub);
        while (!source.hasClients());
        assertTrue(source.hasClients());

        source.set(11.4);
        while (!sink.isNewTemperatureAvailable());
        assertEquals(11.4, sink.getCurrentTemperature());

        broker.closeConnection();
    }

    @Test
    void sourceAndTwoSinksCanCommunicate() {
        broker = new HivemqBroker(DOMAIN, IP, PORT);

        TemperatureSource temperatureSource = createTemperatureSource();
        TemperatureSink sink1 = createTemperatureSink(CONSUMER + "1");
        TemperatureSink sink2 = createTemperatureSink(CONSUMER + "2");

        while (!temperatureSource.hasClients());
        temperatureSource.set(11.4);
        while (
            !sink1.isNewTemperatureAvailable() ||
            !sink2.isNewTemperatureAvailable()
        );

        assertEquals(11.4, sink1.getCurrentTemperature());
        assertEquals(11.4, sink2.getCurrentTemperature());

        broker.closeConnection();
    }

    // Is this needed? Local Twins don't send hello message
    //    @Test
    //    void statusOnlineIsSendBySourceByStart() throws InterruptedException {
    //        broker = new HivemqBroker(DOMAIN, IP, PORT);
    //
    //        Runnable myRunnable = this::createTemperatureSource;
    //        StatusSubscriber statusSubscriber = createStatusSubscriber();
    //
    //        myRunnable.run();
    //
    //        Thread.sleep(100);
    //        assertEquals(1, statusSubscriber.getStatusMessageCount());
    //        assertTrue(statusSubscriber.getLastData().endsWith("1"));
    //
    //        statusSubscriber.reset();
    //        broker.closeConnection();
    //    }

    @Test
    void statusMessageIncludesSender() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN, IP, PORT);

        Runnable myRunnable = this::createTestTwinOffline;
        StatusSubscriber statusSubscriber = createStatusSubscriber();

        myRunnable.run();

        Thread.sleep(1000);

        String postingData = statusSubscriber.getLastData();
        assertEquals(
            PRODUCER,
            postingData.substring(0, postingData.length() - 2)
        );

        statusSubscriber.reset();
        broker.closeConnection();
    }

    private StatusSubscriber createStatusSubscriber() {
        var sink = new JavaTwin(PRODUCER);
        sink.bind(broker);

        var statusSubscriber = new StatusSubscriber();
        statusSubscriber.bind(broker);

        return statusSubscriber;
    }

    private TemperatureSource createTemperatureSource() {
        JavaTwin source = new JavaTwin(PRODUCER);
        source.bind(broker);

        var temperatureSource = new TemperatureSource();
        temperatureSource.bind(source);

        return temperatureSource;
    }

    private TemperatureSink createTemperatureSink(String local) {
        stub = new TwinStub(PRODUCER);
        stub.bind(broker);

        TemperatureSink tempSink = new TemperatureSink(local + "_sink");
        tempSink.bind(broker);
        tempSink.connectDataSource(stub);

        return tempSink;
    }

    private void createTestTwinOffline() {
        var twinForStatusOffline = new TwinForTestStatusMessage(
            broker,
            PRODUCER
        );
        try {
            twinForStatusOffline.publishStatusOffline();
        } catch (InterruptedException ignored) {}
    }
}
