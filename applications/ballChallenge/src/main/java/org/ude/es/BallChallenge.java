package org.ude.es;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.ude.es.protocol.HivemqBroker;

@SpringBootApplication
public class BallChallenge {

    private static final String MQTT_DOMAIN = "eip://uni-due.de/es";
    public static String BROKER_IP = null;
    public static Integer BROKER_PORT = null;
    public static String HOST_IP;
    public static BallChallengeEndpoint ballChallengeEndpoint = null;

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
            Namespace arguments = BallChallenge.parseArguments(
                    args
            );
            BROKER_IP = arguments.getString("broker_address");
            BROKER_PORT = arguments.getInt("broker_port");
        } catch (ArgumentParserException exception) {
            System.out.println(exception.getMessage());
            System.exit(10);
        }

        String CAMERA_IP = null;
        Integer CAMERA_PORT = null;
        try {
            Namespace arguments = parseCameraArguments(args);
            CAMERA_IP = arguments.getString("camera_address");
            System.out.println(CAMERA_IP);
            CAMERA_PORT = Integer.parseInt(arguments.getString("camera_port"));
        } catch (ArgumentParserException exception) {
            System.out.println(exception.getMessage());
            System.exit(10);
        }

        ballChallengeEndpoint = new BallChallengeEndpoint(CAMERA_IP, CAMERA_PORT);
        ballChallengeEndpoint.bindToCommunicationEndpoint(new HivemqBroker(MQTT_DOMAIN, BROKER_IP, BROKER_PORT));

        SpringApplication.run(BallChallenge.class, args);
    }

    static Namespace parseArguments(String[] args)
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
                .setDefault("localhost");
        brokerSpecification
                .addArgument("-p", "--broker-port")
                .type(Integer.class)
                .help("Broker Port")
                .setDefault(1883);
    }

    private static Namespace parseCameraArguments(String[] args)
            throws ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newFor(
                        "elastic-ai.runtime.applications.ballChallenge"
                )
                .build()
                .defaultHelp(true)
                .description("Start a csv service for the elastic-ai.runtime");
        ArgumentGroup cameraSpecification = parser.addArgumentGroup(
                "Camera Specification"
        );
        cameraSpecification
                .addArgument("--camera-address")
                .help("Camera Address")
                .setDefault("localhost");
        cameraSpecification
                .addArgument("--camera-port")
                .help("Camera Port")
                .setDefault(8081);

        return parser.parseKnownArgs(args, null);
    }
}
