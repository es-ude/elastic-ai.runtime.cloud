package de.ude.es;

import static org.junit.jupiter.api.Assertions.*;

import de.ude.es.comm.CommunicationEndpoint;
import de.ude.es.comm.HivemqBroker;
import de.ude.es.comm.Posting;
import de.ude.es.comm.Subscriber;
import de.ude.es.exampleTwins.TwinWithStatus;
import de.ude.es.sink.TemperatureSink;
import de.ude.es.source.TemperatureSource;
import de.ude.es.twin.TwinStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

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

        private TwinWithStatus localDeviceTwin;
        private TemperatureSource temperatureSource;

        public TwinThatOffersTemperature(HivemqBroker broker, String id) {
            createTwinForLocalDevice(broker, id);
            createTemperatureSource();
        }

        private void createTwinForLocalDevice(HivemqBroker broker, String id) {
            localDeviceTwin = new TwinWithStatus(id);
            localDeviceTwin.bind(broker);
            localDeviceTwin.sendOnlinePosting();
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

        private TwinStub remoteDeviceTwin;
        private TemperatureSink temperatureSink;

        public TwinThatConsumesTemperature(
            HivemqBroker broker,
            String local,
            String remote
        ) {
            createTwinForLocalDevice(broker, local);
            createTwinForRemoteDevice(broker, remote);
            createTemperatureSink();
        }

        private void createTwinForLocalDevice(HivemqBroker broker, String id) {
            var localDeviceTwin = new TwinWithStatus(id);
            localDeviceTwin.bind(broker);
            localDeviceTwin.sendOnlinePosting();
        }

        private void createTwinForRemoteDevice(HivemqBroker broker, String id) {
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

        public boolean isNewTemperatureAvailable() {
            return temperatureSink.isNewTemperatureAvailable();
        }
    }

    public static class StatusSubscriber {

        private static class DataSubscriber implements Subscriber {

            public static int statusMessageCount = 0;
            public static String lastData = "";

            @Override
            public void deliver(Posting posting) {
                lastData = posting.data();
                statusMessageCount++;
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

        public int getStatusMessageCount() {
            return DataSubscriber.statusMessageCount;
        }

        public String getLastData() {
            return DataSubscriber.lastData;
        }

        public void reset() {
            DataSubscriber.statusMessageCount = 0;
            DataSubscriber.lastData = "";
        }
    }

    private HivemqBroker broker;
    private TwinStub it;

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
        sink.unbind();
        while (source.hasClients());
        source.set(9.9);
        Thread.sleep(1000);
        assertEquals(0.0, sink.getCurrent());
        broker.closeConnection();
    }

    @Test
    void communicationCanBeResumed() {
        broker = new HivemqBroker(DOMAIN, IP, PORT);

        TemperatureSource source = createTemperatureSource();
        TemperatureSink sink = createTemperatureSink(CONSUMER);

        while (!source.hasClients());
        sink.unbind();

        while (source.hasClients());
        assertFalse(source.hasClients());

        sink.bind(it);
        while (!source.hasClients());
        assertTrue(source.hasClients());

        source.set(11.4);
        while (!sink.isNewTemperatureAvailable());
        assertEquals(11.4, sink.getCurrent());

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

        assertEquals(11.4, sink1.getCurrent());
        assertEquals(11.4, sink2.getCurrent());

        broker.closeConnection();
    }

    @Test
    void statusOnlineIsSendBySourceByStart() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN, IP, PORT);

        Runnable myRunnable = this::createTemperatureSource;
        StatusSubscriber statusSubscriber = createStatusSubscriber();

        myRunnable.run();

        Thread.sleep(100);
        assertEquals(1, statusSubscriber.getStatusMessageCount());
        assertTrue(statusSubscriber.getLastData().endsWith("1"));

        statusSubscriber.reset();
        broker.closeConnection();
    }

    @Test
    void statusMessageIncludesSender() throws InterruptedException {
        broker = new HivemqBroker(DOMAIN, IP, PORT);

        Runnable myRunnable = this::createTemperatureSource;
        StatusSubscriber statusSubscriber = createStatusSubscriber();

        myRunnable.run();

        Thread.sleep(1000);
        assertEquals(
            "/producer",
            statusSubscriber
                .getLastData()
                .substring(0, statusSubscriber.getLastData().length() - 2)
        );

        statusSubscriber.reset();
        broker.closeConnection();
    }

    private StatusSubscriber createStatusSubscriber() {
        var sink = new TwinWithStatus(PRODUCER);
        sink.bind(broker);

        var statusSubscriber = new StatusSubscriber();
        statusSubscriber.bind(broker);

        return statusSubscriber;
    }

    private TemperatureSource createTemperatureSource() {
        var source = new TwinWithStatus(PRODUCER);
        source.bind(broker);
        source.sendOnlinePosting();

        var temperatureSource = new TemperatureSource();
        temperatureSource.bind(source);

        return temperatureSource;
    }

    private TemperatureSink createTemperatureSink(String local) {
        var sink = new TwinWithStatus(local);
        sink.bind(broker);
        sink.sendOnlinePosting();

        it = new TwinStub(PRODUCER);
        it.bind(broker);
        var tempSink = new TemperatureSink(sink.ID());
        tempSink.bind(it);

        return tempSink;
    }
}
