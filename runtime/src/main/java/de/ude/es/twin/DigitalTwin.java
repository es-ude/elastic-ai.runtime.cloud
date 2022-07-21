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
public class DigitalTwin extends Twin {

    public DigitalTwin(String identifier) {
        super(identifier);
    }

    public void publish(Posting posting) {
        Posting toSend = posting.cloneWithTopicAffix(identifier);
        endpoint.publish(toSend);
    }

    public String ID() {
        return endpoint.ID() + identifier;
    }

}
