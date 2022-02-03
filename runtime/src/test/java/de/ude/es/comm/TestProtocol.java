package de.ude.es.comm;


import de.ude.es.Checker;
import de.ude.es.twin.DigitalTwin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestProtocol {

    private static class ProtocolChecker extends Checker {
        public Protocol protocol;
        public DigitalTwin device;

        public void givenProtocol() {
            protocol = new Protocol(broker);
        }

        public void givenDevice(String id) {
            device = new DigitalTwin(id);
            device.bind(broker);
        }

        public void whenPublishingData(String dataId, String value) {
            String topic = protocol.ID()+PostingType.DATA.topic(dataId);
            expected = new Posting(topic, value);
            protocol.publishData(dataId, value);
        }

        public void whenSubscribingForData(String dataId) {
            protocol.subscribeForData(dataId, subscriber);
        }

        public void whenUnsubscribingFromData(String dataId) {
            protocol.unsubscribeFromData(dataId, subscriber);
        }

        public void whenSubscribingForHeartbeat(String source) {
            protocol.subscribeForHeartbeat(source, subscriber);
        }

        public void whenPublishingHeartbeat(String who) {
            String topic = protocol.ID()+who+PostingType.HEARTBEAT.topic("");
            expected = new Posting(topic, "");
            protocol.publishHeartbeat(who);
        }

        public void whenAskingForDataStart(String data, String receiver) {
            String topic = protocol.ID()+PostingType.START.topic(data);
            expected = new Posting(topic, receiver);
            protocol.publishDataStartRequest(data, receiver);
        }

        public void whenAskingForDataStop(String data, String receiver) {
            String topic = protocol.ID()+PostingType.STOP.topic(data);
            expected = new Posting(topic, receiver);
            protocol.publishDataStopRequest(data, receiver);
        }

        public void whenSendingCommand(String service, String cmd) {
            String topic = protocol.ID()+PostingType.SET.topic(service);
            expected = new Posting(topic, cmd);
            protocol.publishCommand(service, cmd);
        }

        public void whenSendingOnCommand(String service) {
            String topic = protocol.ID()+PostingType.SET.topic(service);
            expected = new Posting(topic, "1");
            protocol.publishOnCommand(service);
        }

        public void whenSendingOffCommand(String service) {
            String topic = protocol.ID()+PostingType.SET.topic(service);
            expected = new Posting(topic, "0");
            protocol.publishOffCommand(service);
        }

        public void whenSubscribingForDataStart(String dataId) {
            protocol.subscribeForDataStartRequest(dataId, subscriber);
        }

    }

    private ProtocolChecker checker;


    @BeforeEach
    void init() {
        checker = new ProtocolChecker();
    }

    @Test
    void weCanReportId() {
        var broker = new Broker("test");
        var protocol = new Protocol(broker);
        assertEquals("test", protocol.ID());
    }

    @Test
    void weCanPublishData() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.givenSubscriptionAtBrokerFor("/DATA/temperature");
        checker.whenPublishingData("/temperature", "13.5");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanSubscribeForData() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.whenSubscribingForData("/light");
        checker.whenPostingIsPublishedAtBroker("/DATA/light", "33");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanUnsubscribeForData() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.whenSubscribingForData("/light");
        checker.whenUnsubscribingFromData("/light");
        checker.whenPostingIsPublishedAtBroker("/DATA/light", "33");
        checker.thenPostingIsNotDelivered();
    }

    @Test
    void weCanSubscribeForHeartbeat() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.whenSubscribingForHeartbeat(checker.broker.ID()+"/twin1234");
        checker.whenPostingIsPublishedAtBroker("/twin1234/HEART", "");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanPublishHeartbeat() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.givenSubscriptionAtBrokerFor("/me/HEART");
        checker.whenPublishingHeartbeat("/me");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanPublishDataStartRequest() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.givenSubscriptionAtBrokerFor("/START/data");
        checker.whenAskingForDataStart("/data", "me");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanSubscribeForDataStartRequest() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.whenSubscribingForDataStart("/data");
        checker.whenPostingIsPublishedAtBroker("/START/data", "me");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanAskToStopSendingData() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.givenSubscriptionAtBrokerFor("/STOP/data");
        checker.whenAskingForDataStop("/data", "me");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanSendACommand() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.givenSubscriptionAtBrokerFor("/SET/led");
        checker.whenSendingCommand("/led", "on");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanSendOnCommand() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.givenSubscriptionAtBrokerFor("/SET/led");
        checker.whenSendingOnCommand("/led");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanSendOffCommand() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.givenSubscriptionAtBrokerFor("/SET/led");
        checker.whenSendingOffCommand("/led");
        checker.thenPostingIsDelivered();
    }

    @Test
    void weCanBindProtocolToDevice() {
        checker.givenBroker();
        checker.givenDevice("/us");
        checker.protocol = new Protocol(checker.device);
        checker.givenSubscriptionAtBrokerFor("/us/SET/led");
        checker.whenSendingOffCommand("/led");
        checker.thenPostingIsDelivered();
    }

}
