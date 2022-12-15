package org.ude.es.twinImplementations;

import org.ude.es.comm.Posting;
import org.ude.es.comm.Subscriber;
import org.ude.es.twinBase.JavaTwin;
import org.ude.es.twinBase.TwinStub;

public class PowerConsumptionTwin extends JavaTwin {

    private float sRamPowerConsumption = -1;
    private float wifiPowerConsumption = -1;

    private volatile boolean receivedWifiPowerConsumption = false;
    private volatile boolean receivedSRamPowerConsumption = false;

    private final WifiValueReceiver wifiValueReceiver;
    private final SRamValueReceiver sramValueReceiver;
    private final TwinStub enV5;

    // WIFI

    private class WifiValueReceiver implements Subscriber {
        @Override
        public void deliver(Posting posting) {
            wifiPowerConsumption = Float.parseFloat(posting.data());
            receivedWifiPowerConsumption = true;
        }
    }

    public PowerConsumptionTwin(String identifier) {
        super(identifier);

        wifiValueReceiver = new WifiValueReceiver();
        sramValueReceiver = new SRamValueReceiver();
        enV5 = new ENv5TwinStub("ENv5");
    }

    @Override
    protected void executeOnBind() {
        enV5.bindToCommunicationEndpoint(endpoint);
    }

    public void requestWifiPowerConsumptionContinuously() {
        enV5.publishDataStartRequest("wifiValue", identifier);
        enV5.subscribeForData("wifiValue", wifiValueReceiver);
    }

    public void stopRequestingWifiPowerConsumptionContinuously() {
        enV5.publishDataStopRequest("wifiValue", identifier);
        enV5.unsubscribeFromData("wifiValue", wifiValueReceiver);
    }

    public float requestWifiPowerConsumptionOnce(float timeOut) {
        receivedWifiPowerConsumption = false;
        long start = System.currentTimeMillis();
        long timeElapsed = 0;
        requestWifiPowerConsumptionContinuously();
        while (!receivedWifiPowerConsumption && timeElapsed < timeOut) {
            long finish = System.currentTimeMillis();
            timeElapsed = finish - start;
        }
        stopRequestingWifiPowerConsumptionContinuously();
        return wifiPowerConsumption;
    }

    public boolean receivedNewWifiPowerConsumptionMeasurement() {
        return receivedWifiPowerConsumption;
    }

    public float getLastWifiPowerConsumption() {
        receivedWifiPowerConsumption = false;
        return wifiPowerConsumption;
    }

    // SRAM

    private class SRamValueReceiver implements Subscriber {
        @Override
        public void deliver(Posting posting) {
            sRamPowerConsumption = Float.parseFloat(posting.data());
            receivedSRamPowerConsumption = true;
        }
    }

    public void requestSRamPowerConsumptionContinuously() {
        enV5.publishDataStartRequest("sRam", identifier);
        enV5.subscribeForData("sRamValue", sramValueReceiver);
    }

    public void stopRequestingSRamPowerConsumptionContinuously() {
        enV5.publishDataStopRequest("sRamValue", identifier);
        enV5.unsubscribeFromData("sRamValue", sramValueReceiver);
    }

    public float requestSRamPowerConsumptionOnce(float timeOut) {
        receivedSRamPowerConsumption = false;
        long start = System.currentTimeMillis();
        long timeElapsed = 0;
        requestSRamPowerConsumptionContinuously();
        while (!receivedSRamPowerConsumption && timeElapsed < timeOut) {
            long finish = System.currentTimeMillis();
            timeElapsed = finish - start;
        }
        stopRequestingSRamPowerConsumptionContinuously();
        return sRamPowerConsumption;
    }

    public boolean receivedNewSRamPowerConsumptionMeasurement() {
        return receivedSRamPowerConsumption;
    }

    public float getLastSRamPowerConsumption() {
        receivedSRamPowerConsumption = false;
        return sRamPowerConsumption;
    }
}
