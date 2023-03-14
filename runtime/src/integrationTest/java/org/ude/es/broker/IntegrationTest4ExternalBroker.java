package org.ude.es.broker;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.ude.es.comm.*;
import org.ude.es.sink.TemperatureSink;
import org.ude.es.source.TemperatureSource;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

@Testcontainers
public class IntegrationTest4ExternalBroker {

    private static final String MQTT_DOMAIN = "eip://uni-due.de/es";
    private static final String BROKER_IP = "localhost";
    private int BROKER_PORT = 1883;
    private static final String PRODUCER_ID = "producer";
    private static final String CONSUMER_ID = "consumer";
    private static final String DATA_ID = "/temp";
    private TwinThatOffersTemperature source;
    private TwinThatConsumesTemperature consumer;

    @Container
    public GenericContainer<?> brokerCont = new GenericContainer<>(
        DockerImageName.parse("eclipse-mosquitto:1.6.14")
    )
        .withExposedPorts(BROKER_PORT);

    @BeforeEach
    void setUp() {
        BROKER_PORT = brokerCont.getFirstMappedPort();
        source =
            new TwinThatOffersTemperature(
                createBrokerWithKeepAlive(PRODUCER_ID),
                PRODUCER_ID
            );
        consumer =
            new TwinThatConsumesTemperature(
                createBrokerWithKeepAlive(CONSUMER_ID),
                CONSUMER_ID,
                PRODUCER_ID
            );
    }

    private static class TwinThatOffersTemperature extends JavaTwin {

        private final TemperatureSource temperatureSource;

        public TwinThatOffersTemperature(
            CommunicationEndpoint broker,
            String id
        ) {
            super(id);
            this.bindToCommunicationEndpoint(broker);

            this.temperatureSource = new TemperatureSource(this, DATA_ID);
        }

        public void setNewTemperatureMeasured(double temperature) {
            temperatureSource.set(temperature);
        }

        public boolean hasClients() {
            return temperatureSource.hasClients();
        }
    }

    private static class TwinThatConsumesTemperature extends JavaTwin {

        TemperatureSink temperatureSink;

        public TwinThatConsumesTemperature(
            CommunicationEndpoint broker,
            String id,
            String resourceId
        ) {
            super(id);
            this.bindToCommunicationEndpoint(broker);
            TwinStub dataSource = new TwinStub(resourceId);
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
        while (!source.hasClients());
        source.setNewTemperatureMeasured(11.6);
        while (!consumer.isNewTemperatureAvailable());
        consumer.checkTemperatureIs(11.6);
        source.setNewTemperatureMeasured(1.7);
        while (!consumer.isNewTemperatureAvailable());
        consumer.checkTemperatureIs(1.7);
    }

    @Test
    void communicationCanBeStopped() throws InterruptedException {
        while (!source.hasClients()) {}
        consumer.temperatureSink.disconnectDataSource();
        while (source.hasClients()) {}
        source.setNewTemperatureMeasured(9.8);
        Thread.sleep(1000);
        consumer.checkTemperatureIs(0.0);
    }

    @Test
    void communicationCanBeResumed() {
        TwinStub stub = consumer.temperatureSink.getDataSource();

        while (!source.hasClients());
        consumer.temperatureSink.disconnectDataSource();
        while (source.hasClients());
        assertFalse(source.hasClients());
        consumer.temperatureSink.connectDataSource(stub);
        while (!source.hasClients());
        assertTrue(source.hasClients());

        source.setNewTemperatureMeasured(11.2);
        while (!consumer.isNewTemperatureAvailable());
        consumer.checkTemperatureIs(11.2);
    }

    @Test
    void sourceAndTwoSinksCanCommunicate() {
        var consumer2 = new TwinThatConsumesTemperature(
            createBrokerWithKeepAlive(CONSUMER_ID + "2"),
            CONSUMER_ID + "2",
            PRODUCER_ID
        );

        while (!source.hasClients());
        source.setNewTemperatureMeasured(11.6);
        while (
            !consumer.isNewTemperatureAvailable() ||
            !consumer2.isNewTemperatureAvailable()
        );

        consumer.checkTemperatureIs(11.6);
        consumer2.checkTemperatureIs(11.6);
    }

    private HivemqBroker createBrokerWithKeepAlive(String clientId) {
        HivemqBroker broker = new HivemqBroker(
            MQTT_DOMAIN,
            BROKER_IP,
            BROKER_PORT
        );
        return broker;
    }
}
