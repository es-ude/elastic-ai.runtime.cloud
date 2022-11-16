package org.ude.es.comm;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import java.nio.ByteBuffer;
import java.util.Dictionary;
import java.util.Hashtable;

public class HivemqBroker implements CommunicationEndpoint {

    private final String clientId;
    private final String mqttDomain;
    private final String brokerIp;
    private final int brokerPort;
    private Mqtt5AsyncClient client;

    public void connectWithoutKeepalive() {
        Mqtt5BlockingClient blockingClient = MqttClient
            .builder()
            .useMqttVersion5()
            .identifier(this.mqttDomain)
            .serverHost(this.brokerIp)
            .serverPort(this.brokerPort)
            .buildBlocking();
        Mqtt5ConnAck connAck = blockingClient.connect();
        client = blockingClient.toAsync();
    }

    public void connectWithKeepaliveAndLwtMessage() {
        Mqtt5BlockingClient blockingClient = MqttClient
            .builder()
            .useMqttVersion5()
            .identifier(this.mqttDomain + this.clientId)
            .serverHost(this.brokerIp)
            .serverPort(this.brokerPort)
            //region LWT message
            .willPublish()
            .topic(
                this.mqttDomain + this.clientId + PostingType.STATUS.topic("")
            )
            .payload((this.clientId + ";0").getBytes())
            .qos(MqttQos.AT_MOST_ONCE)
            .retain(true)
            .applyWillPublish()
            //endregion
            .buildBlocking();
        Mqtt5ConnAck connAck = blockingClient.connect();
        client = blockingClient.toAsync();

        Posting onlineStatus = new Posting(
            PostingType.STATUS.topic(""),
            this.clientId + ";1"
        );
        publish(onlineStatus.cloneWithTopicAffix(this.clientId));
    }

    public HivemqBroker(
        String mqttDomain,
        String brokerIp,
        int brokerPort,
        String clientId
    ) {
        this.clientId = fixClientId(clientId);
        this.mqttDomain = fixDomain(mqttDomain);
        this.brokerIp = brokerIp;
        this.brokerPort = brokerPort;
    }

    private static String fixClientId(String id) {
        if (!id.startsWith("/")) {
            id = "/" + id;
        }
        if (id.endsWith("/")) {
            id = id.substring(0, id.length() - 1);
        }
        return id.strip();
    }

    private static String fixDomain(String domain) {
        if (domain.endsWith("/")) {
            return domain.substring(0, domain.length() - 1);
        }
        return domain.strip();
    }

    @Override
    public void publish(Posting posting) {
        client
            .publishWith()
            .topic(posting.cloneWithTopicAffix(this.mqttDomain).topic())
            .payload(posting.data().getBytes())
            .qos(MqttQos.EXACTLY_ONCE)
            .send()
            .whenComplete(this::onPublishComplete);
    }

    private void onPublishComplete(
        Mqtt5PublishResult pubAck,
        Throwable throwable
    ) {
        if (throwable != null) {
            System.out.println(
                "Publishing failed for\t" + pubAck.getPublish().getTopic()
            );
        } else {
            System.out.println(
                "Published: " +
                unwrapPayload(pubAck.getPublish().getPayload().get()) +
                " to: " +
                pubAck.getPublish().getTopic()
            );
        }
    }

    @Override
    public void subscribe(String topic, Subscriber subscriber) {
        subscribeRaw(this.mqttDomain + topic, subscriber);
    }

    @Override
    public void unsubscribe(String topic, Subscriber subscriber) {
        unsubscribeRaw(this.mqttDomain + topic, subscriber);
    }

    @Override
    public void subscribeRaw(String topic, Subscriber subscriber) {
        client
            .subscribeWith()
            .topicFilter(topic)
            .callback(publish ->
                subscriber.deliver(
                    new Posting(
                        topic,
                        unwrapPayload(publish.getPayload().get())
                    )
                )
            )
            .send()
            .whenComplete(
                ((subAck, throwable) -> onSubscribeComplete(throwable, topic))
            );
    }

    private void onSubscribeComplete(Throwable subFailed, String topic) {
        if (subFailed != null) {
            System.out.println("Subscription failed:\t" + topic);
        } else {
            System.out.println("Subscribed to:\t" + topic);
        }
    }

    private static String unwrapPayload(ByteBuffer payload) {
        byte[] realPayload = new byte[payload.remaining()];
        payload.get(realPayload);
        return new String(realPayload);
    }

    @Override
    public void unsubscribeRaw(String topic, Subscriber subscriber) {
        client
            .unsubscribeWith()
            .topicFilter(topic)
            .send()
            .whenComplete(
                (
                    (unsubAck, throwable) ->
                        onUnsubscribeComplete(throwable, topic)
                )
            );
    }

    public void onUnsubscribeComplete(Throwable throwable, String topic) {
        if (throwable != null) {
            System.out.println("Unsubscription failed for:\t" + topic);
        } else {
            System.out.println("Unsubscribe from:\t" + topic);
        }
    }

    @Override
    public String getId() {
        return this.mqttDomain + this.clientId;
    }

    public Dictionary<String, String> getConfiguration() {
        Dictionary<String, String> config = new Hashtable<>();
        config.put("mqttDomain", this.mqttDomain);
        config.put("brokerIp", this.brokerIp);
        config.put("brokerPort", Integer.toString(this.brokerPort));
        return config;
    }

    public void closeConnection() {
        client.disconnect();
    }
}
