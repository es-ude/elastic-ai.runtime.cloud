package de.ude.es;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import de.ude.es.Clients.ClientList;
import de.ude.es.Clients.MonitorCommunicationEndpoint;
import lombok.Getter;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.ude.es.protocol.HivemqBroker;
import org.ude.es.protocol.requests.DataRequester;

@SpringBootApplication
public class MonitoringServiceApplication {

    private static final String MQTT_DOMAIN = "eip://uni-due.de/es";
    private static final String CLIENT_ID = "monitor";
    private static String BROKER_IP = null;
    private static Integer BROKER_PORT = null;
    public static MonitorCommunicationEndpoint monitorCommunicationEndpoint = null;
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

        monitorCommunicationEndpoint = createMonitorTwin();

        SpringApplication.run(MonitoringServiceApplication.class, args);
    }

    private static Namespace parseArguments(String[] args)
        throws ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newFor(
            "elastic-ai.runtime.monitor"
        )
            .build()
            .defaultHelp(true)
            .description(
                "Service for monitoring clients in the elastic-ai.runtime"
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
            .setDefault("127.0.0.1");
        brokerSpecification
            .addArgument("-p", "--broker-port")
            .type(Integer.class)
            .help("Broker Port")
            .setDefault(1883);
    }

    private static MonitorCommunicationEndpoint createMonitorTwin() {
        MonitorCommunicationEndpoint monitorCommunicationEndpoint =
            new MonitorCommunicationEndpoint(CLIENT_ID);
        monitorCommunicationEndpoint.bindToCommunicationEndpoint(
            new HivemqBroker(MQTT_DOMAIN, BROKER_IP, BROKER_PORT)
        );
        return monitorCommunicationEndpoint;
    }

    public static ClientList getClientList() {
        return monitorCommunicationEndpoint.getClientList();
    }

    public static String getLatestMeasurement(DataRequester dataRequester)
        throws TimeoutException {
        UpdatedValueStorage<String> latestValue = new UpdatedValueStorage<>();
        dataRequester.setDataReceiveFunction(
            data -> handleNewData(latestValue, data)
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
        UpdatedValueStorage<String> latestValue,
        String input
    ) {
        try {
            if (input.contains(";")) {
                input = input.split(";")[0];
            }
            latestValue.setValue(input);
        } catch (Exception exception) {
            System.out.println("Unsupported data format.");
        }
    }

    @Getter
    private static class UpdatedValueStorage<Type> {

        private volatile Type value;
        private volatile boolean updated;

        public UpdatedValueStorage() {
            updated = false;
        }

        public void setValue(Type value) {
            this.value = value;
            this.updated = true;
        }
    }
}
