package de.ude.ies.elastic_ai.broker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.ude.ies.elastic_ai.comm.BrokerMock;
import de.ude.ies.elastic_ai.communicationEndpoints.LocalCommunicationEndpoint;
import de.ude.ies.elastic_ai.communicationEndpoints.RemoteCommunicationEndpoint;
import de.ude.ies.elastic_ai.sink.TemperatureSink;
import de.ude.ies.elastic_ai.source.TemperatureSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This is an integration test class that is also meant to provide an example on
 * how to use the provided API to implement twins that measure temperature and
 * twin that access temperature as data.
 */
public class IntegrationTestForTwinInteraction {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String PRODUCER_ID = "producer";
    private static final String CONSUMER_ID = "consumer";
    private static final String DATA_ID = "temp";
    /**
     * The BrokerMock is an MQTTClient and MQTTBroker dummy to test the Twin
     * on the local machine without the need to start a local MQTT Broker.
     * To function properly it has therefore been passed to every Twin
     * instance, whereas the real CommunicationEndpoint implementation has to
     * have its own instance for every Twin.
     */
    private BrokerMock brokerMock;

    private class TwinThatOffersTemperature extends LocalCommunicationEndpoint {

        private final TemperatureSource temperatureSource;

        public TwinThatOffersTemperature(String id) {
            super(id, "localCE");
            this.bindToCommunicationEndpoint(brokerMock);

            temperatureSource = new TemperatureSource(this, DATA_ID);
        }

        public void setNewTemperatureMeasured(double temperature) {
            temperatureSource.set(temperature);
        }
    }

    private class TwinThatConsumesTemperature {

        private TemperatureSink temperatureSink;

        public TwinThatConsumesTemperature(String id, String resourceId) {
            RemoteCommunicationEndpoint dataSource = new RemoteCommunicationEndpoint(resourceId);
            dataSource.bindToCommunicationEndpoint(brokerMock);

            this.temperatureSink = new TemperatureSink(id, DATA_ID);
            temperatureSink.connectDataSource(dataSource);
        }

        public void checkTemperatureIs(double expected) {
            assertEquals(expected, temperatureSink.getCurrentTemperature());
        }
    }

    @BeforeEach
    void setUpTest() {
        brokerMock = new BrokerMock(DOMAIN);
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
        var consumingDevice = new TwinThatConsumesTemperature(CONSUMER_ID, PRODUCER_ID);

        sensingDevice.setNewTemperatureMeasured(11.6);
        consumingDevice.checkTemperatureIs(11.6);
        // ...
        sensingDevice.setNewTemperatureMeasured(1.7);
        consumingDevice.checkTemperatureIs(1.7);
    }

    @Test
    void communicationCanBeStopped() {
        var source = new TwinThatOffersTemperature(PRODUCER_ID);
        var consumer = new TwinThatConsumesTemperature(CONSUMER_ID, PRODUCER_ID);

        consumer.temperatureSink.disconnectDataSource();
        source.setNewTemperatureMeasured(9.9);
        assertEquals(0.0, consumer.temperatureSink.getCurrentTemperature());
    }

    @Test
    void communicationCanBeResumed() {
        var source = new TwinThatOffersTemperature(PRODUCER_ID);
        var consumer = new TwinThatConsumesTemperature(CONSUMER_ID, PRODUCER_ID);
        RemoteCommunicationEndpoint stub = consumer.temperatureSink.getDataSource();

        consumer.temperatureSink.disconnectDataSource();
        source.setNewTemperatureMeasured(11.4);
        consumer.checkTemperatureIs(0.0);

        consumer.temperatureSink.connectDataSource(stub);
        source.setNewTemperatureMeasured(11.5);
        consumer.checkTemperatureIs(11.5);
    }
}
