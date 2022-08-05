package de.ude.es.comm;


/**
 * Anything that can be used to send postings or subscribe for them.
 */
public interface CommunicationEndpoint {

    void publish(Posting posting);

    /**
     * Subscribe relative to the ID od the communication endpoint.
     * Use this method if you want to specify a relative topic and
     * let the communication endpoint add a prefix to make it into
     * a full topic.
     *
     * @param topic      the relative topic to match postings against
     * @param subscriber a client to notify if a matching posting is received
     */
    void subscribe(String topic, Subscriber subscriber);

    void unsubscribe(String topic, Subscriber subscriber);

    /**
     * Subscribe directly to the specified topic, without any
     * modification of it!
     * Use this methof if you want to specify a full topic that is
     * not modified in any way by the communication endpoint.
     *
     * @param topic      the absolute/full topic to match postings against
     * @param subscriber a client to notify if a matching posting is received
     */
    void subscribeRaw(String topic, Subscriber subscriber);

    void unsubscribeRaw(String topic, Subscriber subscriber);

    String ID();

}
