package org.ude.es;


import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import org.ude.es.comm.HivemqBroker;
import org.ude.es.twinBase.DataRequester;
import org.ude.es.twinBase.TwinStub;
import org.ude.es.twinImplementations.MonitorTwin;
import org.ude.es.twinImplementations.enV5Twin;


public class Main {

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static String BROKER_IP = null;
    private static Integer BROKER_PORT = null;

    public static void main(String[] args) throws InterruptedException {
        try {
            Namespace arguments = parseArguments(args);
            BROKER_IP = arguments.getString("broker_address");
            BROKER_PORT = Integer.parseInt(arguments.getString("broker_port"));
        } catch (ArgumentParserException exception) {
            System.out.println(exception.getMessage());
            System.exit(10);
        }

        enV5Twin enV5Twin = new enV5Twin("enV5");
        HivemqBroker broker = new HivemqBroker(DOMAIN, BROKER_IP, BROKER_PORT, enV5Twin.getIdentifier());
        enV5Twin.bindToCommunicationEndpoint(broker);

        while (true) {
            Thread.sleep(10000);
        }
    }

    private static Namespace parseArguments(String[] args) throws ArgumentParserException {
        ArgumentParser parser = ArgumentParsers
                .newFor("elastic-ai.runtime.demo")
                .build()
                .defaultHelp(true)
                .description(
                        "Start a demo twin for the elastic-ai.runtime"
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
}
