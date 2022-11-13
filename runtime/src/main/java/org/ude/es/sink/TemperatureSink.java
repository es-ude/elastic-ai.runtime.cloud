package org.ude.es.sink;

import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

/**
 * A sink representing a temperature that is measured somewhere, e.g., by a
 * remote device. To use it, you need to bind it to a Protocol that allows you
 * to communicate with the remote device that actually measures the
 * temperature.
 */
public class TemperatureSink {

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
    private TwinStub dataSource;
    private final JavaTwin twin;

    public TemperatureSink(JavaTwin twin, String dataId) {
        this.twin = twin;
        this.dataId = dataId;
    }

    public void connectDataSource(TwinStub dataSource) {
        this.dataSource = dataSource;
        this.subscriber = new DataSubscriber();
        this.dataSource.subscribeForData(dataId, subscriber);
        this.dataSource.publishDataStartRequest(dataId, twin.ID());
    }

    public void disconnectDataSource() {
        this.dataSource.unsubscribeFromData(dataId, this.subscriber);
        this.dataSource.publishDataStopRequest(dataId, twin.ID());
        this.dataSource = null;
    }

    public synchronized void setTemperatureAvailable(boolean availability) {
        newTemperatureAvailable = availability;
    }

    public boolean isNewTemperatureAvailable() {
        return newTemperatureAvailable;
    }

    public TwinStub getDataSource() {
        return this.dataSource;
    }

    public Double getCurrentTemperature() {
        setTemperatureAvailable(false);
        return temperature;
    }
}
