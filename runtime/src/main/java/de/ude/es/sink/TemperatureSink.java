package de.ude.es.sink;

import de.ude.es.comm.Posting;
import de.ude.es.comm.Subscriber;
import de.ude.es.twin.TwinStub;


/**
 * A sink representing a temperature that is measured somewhere,
 * e.g., by a remote device.
 * To use it, you need to bind it to a Protocol that allows you
 * to communicate with the remote device that actually measures
 * the temperature.
 */
public class TemperatureSink {

    private TwinStub twin;

    private class DataSubscriber implements Subscriber {

        @Override
        public void deliver(Posting posting) {

            temperature = Double.parseDouble(posting.data());
            setTemperatureAvailable(true);

        }
    }

    private final String dataId;
    private final String localId;
    private double temperature = 0.0;
    private DataSubscriber subscriber;
    private volatile boolean newTemperatureAvailable = false;

    public TemperatureSink(String localTwinId) {
        this.dataId = "temperature";
        this.localId = localTwinId;
    }

    public synchronized void setTemperatureAvailable(boolean availability) {
        newTemperatureAvailable = availability;
    }

    public boolean isNewTemperatureAvailable() {
        return newTemperatureAvailable;
    }

    public void bind(TwinStub twin) {
        this.twin = twin;
        this.subscriber = new DataSubscriber();
        this.twin.subscribeForData(dataId, subscriber);
        this.twin.publishDataStartRequest(dataId, localId);
    }

    public void unbind() {
        twin.unsubscribeFromData(dataId, subscriber);
        twin.publishDataStopRequest(dataId, localId);
    }

    public Double getCurrent() {
        setTemperatureAvailable(false);
        return temperature;
    }

}
