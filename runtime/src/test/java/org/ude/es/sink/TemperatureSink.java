package org.ude.es.sink;

import org.ude.es.protocol.Posting;
import org.ude.es.protocol.Subscriber;
import org.ude.es.communicationEndpoints.LocalCommunicationEndpoint;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;

/**
 * A sink representing a temperature that is measured somewhere, e.g., by a
 * remote device. To use it, you need to bind it to a Protocol that allows you
 * to communicate with the remote device that actually measures the
 * temperature.
 */
public class TemperatureSink extends LocalCommunicationEndpoint {

    private class DataSubscriber implements Subscriber {

        @Override
        public void deliver(Posting posting) {
            temperature = Double.parseDouble(posting.data());
            setTemperatureAvailable(true);
        }
    }

    private double temperature = 0.0;
    private volatile boolean newTemperatureAvailable = false;
    private final String dataId;
    private DataSubscriber subscriber;
    private RemoteCommunicationEndpoint dataSource;

    public TemperatureSink(String twinID, String dataId) {
        super(twinID);
        this.dataId = dataId;
    }

    public void connectDataSource(RemoteCommunicationEndpoint dataSource) {
        this.dataSource = dataSource;
        this.subscriber = new DataSubscriber();
        this.dataSource.subscribeForData(dataId, subscriber);
        this.dataSource.publishDataStartRequest(dataId, this.identifier);
    }

    public void disconnectDataSource() {
        this.dataSource.unsubscribeFromData(dataId);
        this.dataSource.publishDataStopRequest(dataId, this.identifier);
        this.dataSource = null;
    }

    public synchronized void setTemperatureAvailable(boolean availability) {
        newTemperatureAvailable = availability;
    }

    public boolean isNewTemperatureAvailable() {
        return newTemperatureAvailable;
    }

    public RemoteCommunicationEndpoint getDataSource() {
        return this.dataSource;
    }

    public Double getCurrentTemperature() {
        setTemperatureAvailable(false);
        return temperature;
    }
}
