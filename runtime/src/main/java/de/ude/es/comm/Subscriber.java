package de.ude.es.comm;

/**
 * An interface for clients that want to subscribe to postings
 */
public interface Subscriber {
    void deliver(Posting posting);
}
