package de.ude.ies.elastic_ai;

import de.ude.ies.elastic_ai.Clients.ClientList;
import de.ude.ies.elastic_ai.protocol.HivemqBroker;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Monitor {

    private static final String MQTT_DOMAIN = "eip://uni-due.de/es";
    private static final String CLIENT_ID = "monitor";
    public static String BROKER_IP = null;
    public static Integer BROKER_PORT = null;
    public static MonitorCommunicationEndpoint monitorCommunicationEndpoint = null;
    public static String HOST_IP;
    public static Integer PORT;

    public static void main(String[] args) {
        HOST_IP = System.getenv("HOST_IP");
        if (HOST_IP == null) {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                HOST_IP = socket.getLocalAddress().getHostAddress();
            } catch (UnknownHostException | SocketException e) {
                throw new RuntimeException(e);
            }
        }
        HOST_IP = HOST_IP.strip();

        try {
            Namespace arguments = Monitor.parseArguments(args);
            BROKER_IP = arguments.getString("broker_address");
            BROKER_PORT = arguments.getInt("broker_port");
            PORT = arguments.getInt("port");
        } catch (ArgumentParserException exception) {
            System.out.println(exception.getMessage());
            System.exit(10);
        }

        monitorCommunicationEndpoint = createMonitorTwin();
        SpringApplication app = new SpringApplication(Monitor.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", PORT));
        app.run(args);
    }

    static Namespace parseArguments(String[] args) throws ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newFor("elastic-ai.runtime.monitor")
            .build()
            .defaultHelp(true)
            .description("Service for monitoring clients in the elastic-ai.runtime");
        parseBrokerArguments(parser);
        parsePort(parser);
        return parser.parseArgs(args);
    }

    private static void parsePort(ArgumentParser parser) {
        ArgumentGroup brokerSpecification = parser.addArgumentGroup("MQTT Broker Specification");

        brokerSpecification
            .addArgument("--port")
            .type(Integer.class)
            .help("Website Port")
            .setDefault(80);
    }

    private static void parseBrokerArguments(ArgumentParser parser) {
        ArgumentGroup brokerSpecification = parser.addArgumentGroup("MQTT Broker Specification");

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

    static MonitorCommunicationEndpoint createMonitorTwin() {
        System.out.println("Creating MonitorTwin");
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
}
