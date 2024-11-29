package de.ude.ies.elastic_ai.communicationEndpoints;

import de.ude.ies.elastic_ai.protocol.*;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentGroup;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class LocalCommunicationEndpoint extends CommunicationEndpoint {

    protected Status status;

    public LocalCommunicationEndpoint(String identifier, String clientType) {
        super(identifier);
        status = new Status().ID(this.getIdentifier()).TYPE(clientType);
    }

    protected void executeOnBindPrivate() {
        Status lwtMessage = status.STATE(Status.State.OFFLINE).copy();

        if (!this.broker.isConnected()) {
            this.broker.connect(this.identifier, lwtMessage.get());
        }

        publishStatus(status.STATE(Status.State.ONLINE));
        super.executeOnBindPrivate();
    }

    public void publishData(String dataId, String value) {
        this.publish(Posting.createData(dataId, value));
    }

    public void publishStatus(Status status) {
        this.publish(Posting.createStatus(status.ID(this.identifier).get()), true);
    }

    public void publishDone(String command, String value) {
        this.publish(Posting.createDone(command, value));
    }

    public void subscribeForDataStartRequest(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.START.topic(dataId), subscriber);
    }

    public void unsubscribeFromDataStartRequest(String dataId) {
        this.unsubscribe(PostingType.START.topic(dataId));
    }

    public void subscribeForDataStopRequest(String dataId, Subscriber subscriber) {
        this.subscribe(PostingType.STOP.topic(dataId), subscriber);
    }

    public void unsubscribeFromDataStopRequest(String dataId) {
        this.unsubscribe(PostingType.STOP.topic(dataId));
    }

    public void subscribeForCommand(String dataId, Subscriber subscriber) {
        System.out.println(dataId);
        this.subscribe(PostingType.COMMAND.topic(dataId), subscriber);
    }

    public void unsubscribeFromCommand(String dataId) {
        this.unsubscribe(PostingType.COMMAND.topic(dataId));
    }

    public void bindStub(RemoteCommunicationEndpoint stub) {
        stub.bindToCommunicationEndpoint(broker);
    }

    private static final String DOMAIN = "eip://uni-due.de/es";
    private static String BROKER_IP = null;
    private static Integer BROKER_PORT = null;

    public static void startCommunicationEndpoint(
        CommunicationEndpoint communicationEndpoint,
        String[] args
    ) throws InterruptedException {
        try {
            Namespace arguments = parseArguments(args);
            BROKER_IP = arguments.getString("broker_address");
            BROKER_PORT = Integer.parseInt(arguments.getString("broker_port"));
        } catch (ArgumentParserException exception) {
            System.out.println(exception.getMessage());
            System.exit(10);
        }

        HivemqBroker broker = new HivemqBroker(DOMAIN, BROKER_IP, BROKER_PORT);
        communicationEndpoint.bindToCommunicationEndpoint(broker);

        Thread.sleep(3000);
    }

    private static Namespace parseArguments(String[] args) throws ArgumentParserException {
        ArgumentParser parser = ArgumentParsers.newFor("elastic-ai.runtime")
            .build()
            .defaultHelp(true)
            .description("Start a client for the elastic-ai.runtime");
        ArgumentGroup brokerSpecification = parser.addArgumentGroup("MQTT Broker Specification");
        brokerSpecification
            .addArgument("-b", "--broker-address")
            .help("Broker Address")
            .setDefault("localhost");
        brokerSpecification.addArgument("-p", "--broker-port").help("Broker Port").setDefault(1883);

        return parser.parseKnownArgs(args, null);
    }
}
