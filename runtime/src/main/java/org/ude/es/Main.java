package org.ude.es;

import org.ude.es.comm.HivemqBroker;
import org.ude.es.twinImplementations.PowerConsumptionTwin;
import sun.misc.Signal;

public class Main {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final String IP = "localhost";
    private static final int PORT = 1883;

    public static void main(String[] args) {
        PowerConsumptionTwin powerConsumptionTwin = new PowerConsumptionTwin("powerConsumptionTwin");
        HivemqBroker broker = new HivemqBroker(DOMAIN, IP, PORT, powerConsumptionTwin.getIdentifier());
        powerConsumptionTwin.bindToCommunicationEndpoint(broker);

        Signal.handle(new Signal("INT"),  signal -> powerConsumptionTwin.stopRequestingSRamPowerConsumptionContinuously());
        powerConsumptionTwin.requestSRamPowerConsumptionContinuously();

        while (true) {
            if (powerConsumptionTwin.receivedNewSRamPowerConsumptionMeasurement()) {
                System.out.println(powerConsumptionTwin.getLastSRamPowerConsumption());
            }
        }
    }
}
