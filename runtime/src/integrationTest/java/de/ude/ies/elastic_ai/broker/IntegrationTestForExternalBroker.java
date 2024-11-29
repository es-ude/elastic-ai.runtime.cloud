package de.ude.ies.elastic_ai.broker;

import static org.junit.jupiter.api.Assertions.*;

import de.ude.ies.elastic_ai.communicationEndpoints.LocalCommunicationEndpoint;
import de.ude.ies.elastic_ai.communicationEndpoints.RemoteCommunicationEndpoint;
import de.ude.ies.elastic_ai.protocol.BrokerStub;
import de.ude.ies.elastic_ai.protocol.HivemqBroker;
import de.ude.ies.elastic_ai.sink.TemperatureSink;
import de.ude.ies.elastic_ai.source.TemperatureSource;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class IntegrationTestForExternalBroker {

    private static final String MQTT_DOMAIN = "eip://uni-due.de/es";
    private static final String BROKER_IP = "localhost";
    private int BROKER_PORT = 1883;
    private static final String PRODUCER_ID = "producer";
    private static final String CONSUMER_BASE_ID = "consumer";
    private static final String DATA_ID = "/temp";
    private TwinThatOffersTemperature producer;
    private TwinThatConsumesTemperature consumer1;

    @Container
    public HiveMQContainer BROKER_CONTAINER = new HiveMQContainer(
        DockerImageName.parse("hivemq/hivemq-ce").withTag("2023.4")
    )
        .withLogLevel(Level.INFO)
        .withExposedPorts(BROKER_PORT)
        .withReuse(false)
        .withStartupTimeout(Duration.ofMinutes(3));

    @BeforeEach
    void setUp() throws InterruptedException {
        BROKER_PORT = BROKER_CONTAINER.getFirstMappedPort();
        producer = new TwinThatOffersTemperature(
            createBrokerWithKeepAlive(PRODUCER_ID),
            PRODUCER_ID
        );
        consumer1 = new TwinThatConsumesTemperature(
            createBrokerWithKeepAlive(CONSUMER_BASE_ID + "1"),
            CONSUMER_BASE_ID + "1",
            PRODUCER_ID
        );
    }

    private static class TwinThatOffersTemperature extends LocalCommunicationEndpoint {

        private final TemperatureSource temperatureSource;

        public TwinThatOffersTemperature(BrokerStub broker, String id) {
            super(id, "localCE");
            this.bindToCommunicationEndpoint(broker);

            this.temperatureSource = new TemperatureSource(this, DATA_ID);
        }

        public void setNewTemperatureMeasured(double temperature) {
            temperatureSource.set(temperature);
        }

        public boolean hasClients() {
            return temperatureSource.hasClients();
        }

        public boolean hasClients(int numberOfRequiredClients) {
            return (temperatureSource.getNumberOfClients() == numberOfRequiredClients);
        }
    }

    private static class TwinThatConsumesTemperature extends LocalCommunicationEndpoint {

        TemperatureSink temperatureSink;

        public TwinThatConsumesTemperature(BrokerStub broker, String id, String resourceId) {
            super(id, "localCE");
            this.bindToCommunicationEndpoint(broker);
            RemoteCommunicationEndpoint dataSource = new RemoteCommunicationEndpoint(resourceId);
            bindStub(dataSource);

            this.temperatureSink = new TemperatureSink(id, DATA_ID);
            this.temperatureSink.bindToCommunicationEndpoint(broker);
            this.temperatureSink.connectDataSource(dataSource);
        }

        public void checkTemperatureIs(double expected) {
            assertEquals(expected, temperatureSink.getCurrentTemperature());
        }

        public boolean isNewTemperatureAvailable() {
            return this.temperatureSink.isNewTemperatureAvailable();
        }
    }

    /**
     * This is an integration test that shows the basic interaction between
     * twins by exchanging a temperature that is measured by one device
     * ("producer") with another device ("consumer") that wants to access it.
     * The example shows how to achieve this with - a TemperatureSource on the
     * producer side - and a TemperatureSink on the consumer side.
     */
    @Test
    void twinsCanCommunicate() {
        while (!producer.hasClients());
        producer.setNewTemperatureMeasured(11.6);
        while (!consumer1.isNewTemperatureAvailable());
        consumer1.checkTemperatureIs(11.6);
        producer.setNewTemperatureMeasured(1.7);
        while (!consumer1.isNewTemperatureAvailable());
        consumer1.checkTemperatureIs(1.7);
    }

    @Test
    void communicationCanBeStopped() throws InterruptedException {
        while (!producer.hasClients()) {}
        consumer1.temperatureSink.disconnectDataSource();
        while (producer.hasClients()) {}
        producer.setNewTemperatureMeasured(9.8);
        Thread.sleep(1000);
        consumer1.checkTemperatureIs(0.0);
    }

    @Test
    void communicationCanBeResumed() {
        RemoteCommunicationEndpoint stub = consumer1.temperatureSink.getDataSource();

        while (!producer.hasClients());
        consumer1.temperatureSink.disconnectDataSource();
        while (producer.hasClients());
        assertFalse(producer.hasClients());
        consumer1.temperatureSink.connectDataSource(stub);
        while (!producer.hasClients());
        assertTrue(producer.hasClients());

        producer.setNewTemperatureMeasured(11.2);
        while (!consumer1.isNewTemperatureAvailable());
        consumer1.checkTemperatureIs(11.2);
    }

    @Test
    void sourceAndTwoSinksCanCommunicate() {
        TwinThatConsumesTemperature consumer2 = new TwinThatConsumesTemperature(
            createBrokerWithKeepAlive(CONSUMER_BASE_ID + "2"),
            CONSUMER_BASE_ID + "2",
            PRODUCER_ID
        );

        while (!producer.hasClients(2));
        producer.setNewTemperatureMeasured(11.6);
        while (!consumer1.isNewTemperatureAvailable() || !consumer2.isNewTemperatureAvailable());

        consumer1.checkTemperatureIs(11.6);
        consumer2.checkTemperatureIs(11.6);
    }

    private HivemqBroker createBrokerWithKeepAlive(String clientId) {
        HivemqBroker broker = new HivemqBroker(MQTT_DOMAIN, BROKER_IP, BROKER_PORT);
        return broker;
    }
}
