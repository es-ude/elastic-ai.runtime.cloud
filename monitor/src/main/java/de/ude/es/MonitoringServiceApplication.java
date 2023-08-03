package de.ude.es;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.ude.es.comm.HivemqBroker;
import org.ude.es.protocol.DataRequester;

@SpringBootApplication
public class MonitoringServiceApplication {

    private static final String MQTT_DOMAIN = "eip://uni-due.de/es";
    private static final String TWIN_ID = "monitor";
    private static String BROKER_IP = null;
    private static Integer BROKER_PORT = null;
    static MonitorTwin monitor = null;
    public static String IP_ADDRESS;

    public static void main(String[] args) {
        IP_ADDRESS = System.getenv("HOST_IP");
        if (IP_ADDRESS == null) {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                IP_ADDRESS = socket.getLocalAddress().getHostAddress();
            } catch (UnknownHostException | SocketException e) {
                throw new RuntimeException(e);
            }
        }
        IP_ADDRESS = IP_ADDRESS.strip();

        try {
            Namespace arguments = parseArguments(args);
            BROKER_IP = arguments.getString("broker_address");
            BROKER_PORT = arguments.getInt("broker_port");
        } catch (ArgumentParserException exception) {
            System.out.println(exception.getMessage());
            System.exit(10);
        }

        monitor = createMonitorTwin();

        SpringApplication.run(MonitoringServiceApplication.class, args);
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
        defineBrokerArgumentGroup(parser);
        return parser.parseArgs(args);
    }

    private static void defineBrokerArgumentGroup(ArgumentParser parser) {
        ArgumentGroup brokerSpecification = parser.addArgumentGroup(
            "MQTT Broker Specification"
        );
        brokerSpecification
            .addArgument("-b", "--broker-address")
            .help("Broker Address")
            .setDefault("localhost");
        brokerSpecification
            .addArgument("-p", "--broker-port")
            .type(Integer.class)
            .help("Broker Port")
            .setDefault(1883);
    }

    private static MonitorTwin createMonitorTwin() {
        MonitorTwin monitor = new MonitorTwin(TWIN_ID);
        monitor.bindToCommunicationEndpoint(
            new HivemqBroker(MQTT_DOMAIN, BROKER_IP, BROKER_PORT)
        );
        return monitor;
    }

    public static TwinList getTwinList() {
        return monitor.getTwinList();
    }

    public static float getLatestMeasurement(DataRequester dataRequester) throws TimeoutException {
        UpdatedValueStorage<Float> latestValue = new UpdatedValueStorage<>();
        dataRequester.setDataReceiveFunction(data ->
            handleNewData(latestValue, data)
        );
        dataRequester.startRequestingData();

        long start = System.currentTimeMillis();
        long end = start + 5000;
        while (!latestValue.isUpdated()) {
            if (System.currentTimeMillis() >= end) {
                throw new TimeoutException("No Message Received");
            }
        }

        return latestValue.getValue();
    }

    private static void handleNewData(
        UpdatedValueStorage<Float> latestValue,
        String input
    ) {
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
            this.value = value;
            this.updated = true;
        }
    }
}
