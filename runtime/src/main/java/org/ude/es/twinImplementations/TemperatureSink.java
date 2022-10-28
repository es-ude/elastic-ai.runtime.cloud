package org.ude.es.twinImplementations;

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
public class TemperatureSink extends JavaTwin {

    private class DataSubscriber implements Subscriber {

        @Override
        public void deliver(Posting posting) {
            temperature = Double.parseDouble(posting.data());
            setTemperatureAvailable(true);
        }
    }

    private double temperature = 0.0;
    private volatile boolean newTemperatureAvailable = false;
    private String dataId = "TEMP";
    private DataSubscriber subscriber;
    private TwinStub sensorTwin;

    public TemperatureSink(String sinkId) {
        super(sinkId);
    }

    public TemperatureSink(String sinkId, String dataId) {
        super(sinkId);
        this.dataId = dataId;
    }

    public void connectDataSource(TwinStub dataSource) {
        this.sensorTwin = dataSource;
        this.subscriber = new DataSubscriber();
        System.out.println(
            "DATA_ID: " + dataId + ", DATA_SRC: " + dataSource.ID()
        );
        this.sensorTwin.subscribeForData(dataId, subscriber);
        this.sensorTwin.publishDataStartRequest(dataId, this.identifier);
    }

    public void disconnectDataSource() {
        this.sensorTwin.unsubscribeFromData(dataId, this.subscriber);
        this.sensorTwin.publishDataStopRequest(dataId, this.identifier);
    }

    public synchronized void setTemperatureAvailable(boolean availability) {
        newTemperatureAvailable = availability;
    }

    public boolean isNewTemperatureAvailable() {
        return newTemperatureAvailable;
    }

    public Double getCurrentTemperature() {
        setTemperatureAvailable(false);
        return temperature;
    }
}
