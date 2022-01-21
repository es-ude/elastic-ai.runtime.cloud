package de.ude.es.twin;


import de.ude.es.comm.CommunicationEndpoint;
import de.ude.es.comm.Posting;
import de.ude.es.comm.Subscriber;

/**
 * This is the base class for all digital twins, i.e., local
 * entities that represent some remote object, device, room
 * or measured phenomenon like temperature or sound.
 * It provides the ability to bind your twin to a data source
 * via a CommunicationEndpoint! Everything else has to be
 * added by subclasses.
 */
public class DigitalTwin implements CommunicationEndpoint {

    protected final String identifier;
    protected CommunicationEndpoint endpoint;


    public DigitalTwin(String identifier) {
        this.identifier = fixIdentifierIfNecessary(identifier);
    }

    private String fixIdentifierIfNecessary(String identifier) {
        if(!identifier.startsWith("/"))
            identifier = "/"+identifier;
        if(identifier.endsWith("/"))
            identifier = identifier.substring(0, identifier.length()-1);
        return identifier;
    }

    /**
     * Call this method to bind your DigitalTwin to a
     * CommunicationEndpoint, to send / receive postings.
     * Note: this method cannot be overwritten! If you
     * want your DigitalTwin subclass to perform its own
     * actions when binding, please override @executeOnBind.
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
    protected void executeOnBind() {
    }

    @Override
    public void publish(Posting posting) {
        Posting toSend = posting.cloneWithTopicAffix(identifier);
        endpoint.publish(toSend);
    }

    @Override
    public void subscribe(String topic, Subscriber subscriber) {
        endpoint.subscribe(identifier + topic, subscriber);
    }

    @Override
    public void subscribeRaw(String topic, Subscriber subscriber) {
        endpoint.subscribeRaw(topic, subscriber);
    }

    @Override
    public void unsubscribeRaw(String topic, Subscriber subscriber) {
        endpoint.unsubscribeRaw(topic, subscriber);
    }

    @Override
    public void unsubscribe(String topic, Subscriber subscriber) {
        endpoint.unsubscribe(topic, subscriber);
    }

    @Override
    public String ID() {
        return endpoint.ID()+identifier;
    }


}
