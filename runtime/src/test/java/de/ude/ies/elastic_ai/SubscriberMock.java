package de.ude.ies.elastic_ai;

import static org.junit.jupiter.api.Assertions.*;

import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.Subscriber;
import java.util.ArrayList;

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
        fail("posting " + expected + " should have delivered (delivered topics: " + topics + ")");
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
        for (Posting p : deliveredPostings) topics.append(p.topic()).append(", ");
        assertEquals(amount, deliveredPostings.size(), topics.toString());
    }

    public void clearPostings() {
        deliveredPostings.clear();
    }
}
