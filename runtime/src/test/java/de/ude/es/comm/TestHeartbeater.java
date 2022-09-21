package de.ude.es.comm;

import de.ude.es.Checker;
import de.ude.es.TimerMock;
import de.ude.es.twin.JavaTwin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestHeartbeater {

    private static class HeartbeaterChecker extends Checker {

        public JavaTwin javaTwin;
        public TimerMock timer;
        public Heartbeater heartbeater;

        public void givenProtocol() {
            javaTwin = new JavaTwin("test123");
            javaTwin.bind(broker);
        }

        public void givenHeartbeater() {
            timer = new TimerMock();
            heartbeater = new Heartbeater(javaTwin, "/test123", timer, 1000);
        }

        public void whenStartingPeriodicHeartbeats() {
            heartbeater.start();
        }

        public void whenTimerTicked() {
            timer.fire();
        }

        public void thenMultiplePostingsAreDelivered(int amount) {
            subscriber.checkNumberOfPostingsDelivered(amount);
        }

        public void whenStoppingPeriodicHeartbeats() {
            heartbeater.stop();
        }
    }

    private HeartbeaterChecker checker;

    @BeforeEach
    void init() {
        checker = new HeartbeaterChecker();
    }

    @Test
    void weCanPublishHeartbeatPeriodically() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.givenHeartbeater();
        checker.givenSubscriptionAtBrokerFor("/test123/HEART");

        checker.whenStartingPeriodicHeartbeats();
        checker.thenMultiplePostingsAreDelivered(1);
        checker.whenTimerTicked();
        checker.thenMultiplePostingsAreDelivered(2);
        checker.whenTimerTicked();
        checker.thenMultiplePostingsAreDelivered(3);
    }

    @Test
    void weCanStopPublishingHeartbeatsPeriodically() {
        checker.givenBroker();
        checker.givenProtocol();
        checker.givenSubscriptionAtBrokerFor("/test123/HEART");
        checker.givenHeartbeater();
        checker.whenStartingPeriodicHeartbeats();
        checker.whenTimerTicked();

        checker.whenStoppingPeriodicHeartbeats();
        checker.whenTimerTicked();
        checker.thenMultiplePostingsAreDelivered(2);
    }
}
