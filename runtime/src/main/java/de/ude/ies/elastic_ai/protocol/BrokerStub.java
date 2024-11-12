package de.ude.ies.elastic_ai.protocol;

/**
 * <p>
 * Anything that can be used to send postings or subscribe for them.
 * </p>
 * <p>
 * This Interface should be implemented by a Broker.
 * A broker is the central communication backend for a deployment.
 * It is responsible for managing subscriptions and forwarding postings to the correct clients.
 * It is also responsible for forwarding to the correct subscribers locally on a device!
 * </p>
 * <p>
 * <b>Note:</b> A broker can but doesn't need to be implemented by
 * an MQTT broker.
 * </p>
 */
public interface BrokerStub {
    void connect(String clientId, String lwtMessage);

    boolean isConnected();

    void publish(Posting posting, boolean retain);

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

    void unsubscribe(String topic);

    /**
     * Subscribe directly to the specified topic, without any
     * modification of it!
     * Use this method if you want to specify a full topic that is
     * not modified in any way by the communication endpoint.
     *
     * @param topic      the absolute/full topic to match postings against
     * @param subscriber a client to notify if a matching posting is received
     */
    void subscribeRaw(String topic, Subscriber subscriber);

    void unsubscribeRaw(String topic);

    String getClientIdentifier();

    String getDomain();
}
