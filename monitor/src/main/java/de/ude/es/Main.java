package de.ude.es;


import de.ude.es.comm.HivemqBroker;
import de.ude.es.twin.JavaTwin;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.sql.SQLOutput;
import java.util.Objects;


public class Main {

    private static String BROKER = null;
    private static Integer PORT = null;
    public static TwinList twinList;
    private static final String DOMAIN = "eip://uni-due.de/es";
    private static final int kikTime = 60000;

    public static void main(String[] args) {
        try {
            Namespace arguments = parseArguments(args);
            BROKER = arguments.getString("broker_address");
            PORT = Integer.parseInt(arguments.getString("broker_port"));
        } catch (ArgumentParserException exception) {
            System.out.println(exception.getMessage());
            System.exit(10);
        }

        twinList = new TwinList(kikTime);
        HivemqBroker broker = new HivemqBroker(DOMAIN, BROKER, PORT);

        var sink = new JavaTwin("monitor");
        sink.bind(broker);

        HeartbeatSubscriber heartbeatSubscriber = new HeartbeatSubscriber(twinList);
        heartbeatSubscriber.bind(broker);

        MonitoringServiceApplication serviceApplication = new MonitoringServiceApplication();
        serviceApplication.startServer(args);
    }

    public static Namespace parseArguments(String[] args) throws ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newFor("elastic.ai-monitor")
                .build()
                .defaultHelp(true)
                .description("Service for monitoring digital twins in the elastic-ai.runtime");
        ArgumentGroup brokerSpecification = parser.addArgumentGroup("MQTT Broker Specification");
        brokerSpecification.addArgument("-b", "--broker-address").help("Broker Address").setDefault("localhost");
        brokerSpecification.addArgument("-p", "--broker-port").help("Broker Port").setDefault(1883);

        return parser.parseArgs(args);
    }
}
