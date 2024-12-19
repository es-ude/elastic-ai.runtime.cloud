package de.ude.ies.elastic_ai.protocol;

/**
 * An interface for clients that want to subscribe to postings
 */
public interface Subscriber {
    void deliver(Posting posting) throws InterruptedException;
}
