package de.ude.es;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.ude.es.comm.HivemqBroker;

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

    public static float getMeasurement(
        String deviceId,
        String sensorId,
        String value
    ) {
        // TODO: get value from ENv5 TwinStub
        return -3.5f;
    }

    public static boolean isLED1On(String deviceId) {
        // TODO: get status from ENv5 TwinStub
        return true;
    }

    public static boolean setLED1(String deviceId, boolean on) {
        // TODO: set status for ENv5 TwinStub
        // TODO: return status form ENv5 TwinStub
        return true;
    }

    private static HivemqBroker createBrokerWithKeepalive() {
        HivemqBroker broker = new HivemqBroker(
            MQTT_DOMAIN,
            BROKER_IP,
            BROKER_PORT,
            "monitor"
        );
        return broker;
    }
}
