package org.ude.es;


import org.ude.es.comm.HivemqBroker;
import org.ude.es.twinImplementations.PowerConsumptionTwin;
import sun.misc.Signal;


public class Main {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;

    public static void main(String[] args) throws InterruptedException {
        PowerConsumptionTwin powerConsumptionTwin = new PowerConsumptionTwin("powerConsumptionTwin");
        HivemqBroker broker = new HivemqBroker(DOMAIN, IP, PORT, powerConsumptionTwin.getIdentifier());
        powerConsumptionTwin.bindToCommunicationEndpoint(broker);

        powerConsumptionTwin.sRamValueReceiver.startRequestingData();

        powerConsumptionTwin.wifiValueReceiver.startRequestingData();

        while (true) {
            if (powerConsumptionTwin.sRamValueReceiver.receivedNewValue()) {
                System.out.println("sRAM:" + powerConsumptionTwin.sRamValueReceiver.getLastValue());
            }
            if (powerConsumptionTwin.wifiValueReceiver.receivedNewValue()) {
                System.out.println("Wifi: " + powerConsumptionTwin.wifiValueReceiver.getLastValue());
            }
            Thread.sleep(100);
        }
    }
}
