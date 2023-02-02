package de.ude.es;

import java.util.concurrent.TimeoutException;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.ude.es.comm.HivemqBroker;
import org.ude.es.protocol.DataRequester;
import org.ude.es.twinBase.TwinStub;

public class Main {

    private static String BROKER_IP = null;
    private static Integer BROKER_PORT = null;
    private static final String MQTT_DOMAIN = "eip://uni-due.de/es";
    private static MonitorTwin monitor = null;

    public static void main(String[] args) {
        try {
            Namespace arguments = parseArguments(args);
            BROKER_IP = arguments.getString("broker_address");
            BROKER_PORT = Integer.parseInt(arguments.getString("broker_port"));
        } catch (ArgumentParserException exception) {
            System.out.println(exception.getMessage());
            System.exit(10);
        }

        monitor = new MonitorTwin("monitor");
        monitor.bindToCommunicationEndpoint(createBrokerWithKeepalive());

        monitor.getTwinList().addTwin("env5_1");

        MonitoringServiceApplication serviceApplication = new MonitoringServiceApplication();
        serviceApplication.startServer(args);
    }

    public static TwinList getTwinList() {
        return monitor.getTwinList();
    }

    private static Namespace parseArguments(String[] args)
        throws ArgumentParserException {
        ArgumentParser parser = ArgumentParsers
            .newFor("elastic-ai.runtime.monitor")
            .build()
            .defaultHelp(true)
            .description(
                "Service for monitoring digital twins in the elastic-ai.runtime"
            );
        ArgumentGroup brokerSpecification = parser.addArgumentGroup(
            "MQTT Broker Specification"
        );
        brokerSpecification
            .addArgument("-b", "--broker-address")
            .help("Broker Address")
            .setDefault("localhost");
        brokerSpecification
            .addArgument("-p", "--broker-port")
            .help("Broker Port")
            .setDefault(1883);

        return parser.parseArgs(args);
    }

    public static float getLatestMeasurement(String deviceId, String sensorId)
        throws TimeoutException {
        UpdatedValueStorage<Float> latestValue = new UpdatedValueStorage<>();

        TwinStub deviceStub = new TwinStub(deviceId);
        deviceStub.bindToCommunicationEndpoint(monitor.getEndpoint());

        DataRequester deviceRequest = new DataRequester(
            deviceStub,
            sensorId,
            monitor.getIdentifier()
        );
        deviceRequest.addWhenNewDataReceived(data ->
            handleNewData(deviceRequest, latestValue, data)
        );
        deviceRequest.startRequestingData();

        long start = System.currentTimeMillis();
        long end = start + 60000;
        while (!latestValue.isUpdated()) {
            if (start >= end) {
                throw new TimeoutException("No Message Received");
            }
        }

        return latestValue.getValue();
    }

    private static HivemqBroker createBrokerWithKeepalive() {
        return new HivemqBroker(MQTT_DOMAIN, BROKER_IP, BROKER_PORT, "monitor");
    }

    private static void handleNewData(
        DataRequester requester,
        UpdatedValueStorage<Float> latestValue,
        String input
    ) {
        requester.stopRequestingData();
        latestValue.setValue(Float.parseFloat(input));
    }

    private static class UpdatedValueStorage<Type> {

        private volatile Type value;
        private volatile boolean updated;

        public UpdatedValueStorage() {
            updated = false;
        }

        public boolean isUpdated() {
            return updated;
        }

        public Type getValue() {
            return value;
        }

        public void setValue(Type value) {
            this.updated = true;
            this.value = value;
        }
    }
}
