package org.ude.es;

import org.ude.es.protocol.Posting;
import org.ude.es.protocol.Subscriber;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class SubscriberMock implements Subscriber {

    private final ArrayList<Posting> deliveredPostings = new ArrayList<>();

    @Override
    public void deliver(Posting posting) {
        deliveredPostings.add(posting);
    }

    public void checkPostingDelivered(Posting expected) {
        StringBuilder topics = new StringBuilder();
        for (Posting actual : deliveredPostings) {
            topics.append(actual).append(", ");
            if (expected.equals(actual)) return;
        }
        fail(
            "posting " +
            expected +
            " should have delivered (delivered topics: " +
            topics +
            ")"
        );
    }

    public void checkTopicDelivered(String expected) {
        for (Posting actual : deliveredPostings) {
            if (expected.equals(actual.topic())) return;
        }
        fail("posting with topic " + expected + " should have delivered");
    }

    public void checkNoPostingDelivered() {
        assertTrue(deliveredPostings.isEmpty());
    }

    public void checkNumberOfPostingsDelivered(int amount) {
        StringBuilder topics = new StringBuilder();
        for (Posting p : deliveredPostings) topics
            .append(p.topic())
            .append(", ");
        assertEquals(amount, deliveredPostings.size(), topics.toString());
    }

    public void clearPostings() {
        deliveredPostings.clear();
    }
}
