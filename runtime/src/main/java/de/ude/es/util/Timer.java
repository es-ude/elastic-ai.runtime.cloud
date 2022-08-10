package de.ude.es.util;

/**
 * An interface for timer implementations. These can be platform
 * dependent and, e.g., use multithreading or a command queue for
 * their implementation.
 * Note that a timer must be able to handle multiple timeouts from
 * different clients.
 */
public interface Timer {

    Timeout register(int timeoutMillis, TimerClient client);

}
