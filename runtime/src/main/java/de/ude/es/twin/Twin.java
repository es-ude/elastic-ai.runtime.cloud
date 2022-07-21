package de.ude.es.twin;

import de.ude.es.comm.CommunicationEndpoint;

public class Twin {

    protected final String identifier;
    protected CommunicationEndpoint endpoint;

    public Twin(String identifier) {
        this.identifier = fixIdentifierIfNecessary(identifier);
    }

    private String fixIdentifierIfNecessary(String identifier) {
        if (!identifier.startsWith("/"))
            identifier = "/" + identifier;
        if (identifier.endsWith("/"))
            identifier = identifier.substring(0, identifier.length() - 1);
        return identifier;
    }

    /**
     * Call this method to bind your DigitalTwin to a
     * CommunicationEndpoint, to send / receive postings.
     * Note: this method cannot be overwritten! If you
     * want your DigitalTwin subclass to perform its own
     * actions when binding, please override @executeOnBind.
     *
     * @param channel Where you post messages or subscribe for them
     */
    public final void bind(CommunicationEndpoint channel) {
        this.endpoint = channel;
        executeOnBind();
    }

    /**
     * Overwrite this method if you want to perform
     * actions when you bind this DigitalTwin to
     * a CommunicationEndpoint, e.g., to subscribe
     * for certain topics or notify someone that
     * you are interested in some data.
     */
    protected void executeOnBind() {}

}
