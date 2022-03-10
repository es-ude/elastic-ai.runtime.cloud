package de.ude.es.twin;

import de.ude.es.Checker;
import de.ude.es.TimerMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class TestTwinWithHeartbeat {

    private static class TwinWithHeartbeatChecker extends Checker {
        public TimerMock timer;
        public TwinWithHeartbeat twinWithHeartbeat;

        public void givenTwinWithHeartbeat(String id) {
            twinWithHeartbeat = new TwinWithHeartbeat(id);
            twinWithHeartbeat.bind(broker);
        }

        public void whenStartingPeriodicHeartbeats() {
            timer = new TimerMock();
            twinWithHeartbeat.startHeartbeats(timer, 1000);
        }

        public void whenTimerTicked() {
            timer.fire();
        }

        public void thenMultiplePostingsAreDelivered(int amount) {
            subscriber.checkNumberOfPostingsDelivered(amount);
        }

        public void whenStoppingPeriodicHeartbeats() {
            twinWithHeartbeat.stopHeartbeats();
        }

    }

    private TwinWithHeartbeatChecker checker;


    @BeforeEach
    void init() {
        checker = new TwinWithHeartbeatChecker();
    }

    @Test
    void weCanPublishHeartbeatPeriodically() {
        checker.givenBroker();
        checker.givenTwinWithHeartbeat("/test123");
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
        checker.givenTwinWithHeartbeat("/test123");
        checker.givenSubscriptionAtBrokerFor("/test123/HEART");
        checker.whenStartingPeriodicHeartbeats();
        checker.whenTimerTicked();

        checker.whenStoppingPeriodicHeartbeats();
        checker.whenTimerTicked();
        checker.thenMultiplePostingsAreDelivered(2);
    }

}
