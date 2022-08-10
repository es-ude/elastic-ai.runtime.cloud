package de.ude.es.util;

public interface Timeout {

    /**
     * Note that this method can be called on the Timer even after the timeout has occurred.
     * Doing so starts another timeout with the same TimerClient as before.
     * Calling it before the timeout has occurred resets the timer and the old timeout is deleted
     * without firing a timeout.
     */
    void restart();

    void stop();

}
