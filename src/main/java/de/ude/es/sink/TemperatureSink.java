package de.ude.es.sink;

import de.ude.es.comm.CommunicationEndpoint;
import de.ude.es.comm.Posting;
import de.ude.es.comm.Protocol;
import de.ude.es.comm.Subscriber;


/**
 * A sink representing a temperature that is measured somewhere,
 * e.g., by a remote device.
 * To use it, you need to bind it to a Protocol that allows you
 * to communicate with the remote device that actually measures
 * the temperature.
 */
public class TemperatureSink {

    private class DataSubscriber implements Subscriber {

        @Override
        public void deliver(Posting posting) {

            temperature = Double.parseDouble(posting.data());
            System.out.println("deliver temperature"+temperature);
            setTemperatureAvailable(true);
            System.out.println(isNewTemperatureAvailable());

        }
    }


    private final String dataId;
    private final String localId;
    private double temperature = 0.0;
    private Protocol protocol;
    private DataSubscriber subscriber;
    private volatile boolean newTemperatureAvailable=false;

    public TemperatureSink(String localTwinId) {
        this.dataId = "/temperature";
        this.localId = localTwinId;
    }

    public synchronized void setTemperatureAvailable(boolean availability){
        newTemperatureAvailable=availability;
    }
    public boolean isNewTemperatureAvailable(){
        return newTemperatureAvailable;
    }
    public void bind(CommunicationEndpoint endpoint) {
        bind(new Protocol(endpoint));
    }

    public void bind(Protocol protocol) {
        this.protocol = protocol;
        this.subscriber = new DataSubscriber();
        this.protocol.subscribeForData(dataId, subscriber);
        this.protocol.publishDataStartRequest(dataId, localId);
    }

    public void unbind() {
        protocol.unsubscribeFromData(dataId, subscriber);
        protocol.publishDataStopRequest(dataId, localId);
    }

    public Double getCurrent() {
        setTemperatureAvailable(false);
        return temperature;
    }

}
