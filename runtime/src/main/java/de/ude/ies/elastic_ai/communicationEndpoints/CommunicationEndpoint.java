package de.ude.ies.elastic_ai.communicationEndpoints;

import de.ude.ies.elastic_ai.protocol.BrokerStub;
import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.Subscriber;

public class CommunicationEndpoint {

    protected final String identifier;
    protected BrokerStub broker;

    public interface DataExecutor {
        void execute(String data);
    }

    public interface Executor {
        void execute();
    }

    public CommunicationEndpoint(String identifier) {
        this.identifier = fixIdentifierIfNecessary(identifier);
    }

    private String fixIdentifierIfNecessary(String identifier) {
        if (identifier.startsWith("/")) {
            identifier = identifier.substring(1);
        }
        if (identifier.endsWith("/")) {
            identifier = identifier.substring(0, identifier.length() - 1);
        }
        return identifier.strip();
    }

    protected void subscribe(String topic, Subscriber subscriber) {
        broker.subscribe(identifier + topic, subscriber);
    }

    protected void unsubscribe(String topic) {
        broker.unsubscribe(identifier + topic);
    }

    protected void publish(Posting posting) {
        publish(posting, false);
    }

    protected void publish(Posting posting, boolean retain) {
        Posting toSend = posting.cloneWithTopicAffix(identifier);
        broker.publish(toSend, retain);
    }

    /**
     * <p>
     * Call this method to bind your DigitalTwin to a CommunicationEndpoint, to
     * send and receive postings.
     * </p>
     * <p>
     * <b>Note:</b> This method cannot be overwritten! If you
     * want your DigitalTwin subclass to perform its own
     * actions when binding, please override @executeOnBind.
     * </p>
     * <p>
     * <b>Important:</b> Every Twin must have its own instance of the implemented
     * Communication Interface.
     * </p>
     *
     * @param channel Where you post messages or subscribe for them
     */
    public void bindToCommunicationEndpoint(BrokerStub channel) {
        this.broker = channel;
        executeOnBindPrivate();
    }

    /**
     * Overwrite this method if you want to perform actions when you bind this
     * DigitalTwin to a CommunicationEndpoint, e.g., to subscribe for certain
     * topics or notify someone that you are interested in some data.
     */
    protected void executeOnBind() {}

    protected void executeOnBindPrivate() {
        executeOnBind();
    }

    public String getDomain() {
        return broker.getDomain();
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDomainAndIdentifier() {
        return getDomain() + "/" + getIdentifier();
    }

    public BrokerStub getBroker() {
        return broker;
    }
}
