package org.ude.es.broker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.BrokerMock;
import org.ude.es.sink.TemperatureSink;
import org.ude.es.source.TemperatureSource;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

/**
 * This is an integration test class that is also meant to provide an example on
 * how to use the provided API to implement twins that measure temperature and
 * twin that access temperature as data.
 */
public class IntegrationTest4TwinInteraction {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String PRODUCER_ID = "/producer";
    private static final String CONSUMER_ID = "/consumer";
    private static final String DATA_ID = "/temp";
    /**
     * The BrokerMock is an MQTTClient and MQTTBroker dummy to test the Twin
     * on the local machine without the need to start a local MQTT Broker.
     * To function properly it has therefore been passed to every Twin
     * instance, whereas the real CommunicationEndpoint implementation has to
     * have its own instance for every Twin.
     */
    private BrokerMock broker;

    private class TwinThatOffersTemperature extends JavaTwin {

        private final TemperatureSource temperatureSource;

        public TwinThatOffersTemperature(String id) {
            super(id);
            this.bindToCommunicationEndpoint(broker);

            temperatureSource = new TemperatureSource(this, DATA_ID);
        }

        public void setNewTemperatureMeasured(double temperature) {
            temperatureSource.set(temperature);
        }
    }

    private class TwinThatConsumesTemperature extends JavaTwin {

        private TemperatureSink temperatureSink;

        public TwinThatConsumesTemperature(String id, String resourceId) {
            super(id);
            this.bindToCommunicationEndpoint(broker);

            createTemperatureSink(resourceId);
        }

        private void createTemperatureSink(String resourceId) {
            TwinStub dataSource = new TwinStub(resourceId);
            dataSource.bindToCommunicationEndpoint(broker);

            this.temperatureSink = new TemperatureSink(this, DATA_ID);
            temperatureSink.connectDataSource(dataSource);
        }

        public void checkTemperatureIs(double expected) {
            assertEquals(expected, temperatureSink.getCurrentTemperature());
        }
    }

    @BeforeEach
    void setUpTest() {
        broker = new BrokerMock(DOMAIN);
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
        var sensingDevice = new TwinThatOffersTemperature(PRODUCER_ID);
        var consumingDevice = new TwinThatConsumesTemperature(
            CONSUMER_ID,
            PRODUCER_ID
        );

        sensingDevice.setNewTemperatureMeasured(11.6);
        consumingDevice.checkTemperatureIs(11.6);
        // ...
        sensingDevice.setNewTemperatureMeasured(1.7);
        consumingDevice.checkTemperatureIs(1.7);
    }

    @Test
    void communicationCanBeStopped() {
        var source = new TwinThatOffersTemperature(PRODUCER_ID);
        var consumer = new TwinThatConsumesTemperature(
            CONSUMER_ID,
            PRODUCER_ID
        );

        consumer.temperatureSink.disconnectDataSource();
        source.setNewTemperatureMeasured(9.9);
        assertEquals(0.0, consumer.temperatureSink.getCurrentTemperature());
    }

    @Test
    void communicationCanBeResumed() {
        var source = new TwinThatOffersTemperature(PRODUCER_ID);
        var consumer = new TwinThatConsumesTemperature(
            CONSUMER_ID,
            PRODUCER_ID
        );
        TwinStub stub = consumer.temperatureSink.getDataSource();

        consumer.temperatureSink.disconnectDataSource();
        source.setNewTemperatureMeasured(11.4);
        consumer.checkTemperatureIs(0.0);

        consumer.temperatureSink.connectDataSource(stub);
        source.setNewTemperatureMeasured(11.5);
        consumer.checkTemperatureIs(11.5);
    }

    @Test
    void sourceAndTwoSinksCanCommunicate() {
        var source = new TwinThatOffersTemperature(PRODUCER_ID);
        var consumer1 = new TwinThatConsumesTemperature(
            CONSUMER_ID + "1",
            PRODUCER_ID
        );
        var consumer2 = new TwinThatConsumesTemperature(
            CONSUMER_ID + "2",
            PRODUCER_ID
        );

        source.setNewTemperatureMeasured(11.3);
        consumer1.checkTemperatureIs(11.3);
        consumer2.checkTemperatureIs(11.3);
    }
}
