package de.ude.es.exampleTwins;

import de.ude.es.twin.TwinStub;

/**
 * A digital twin representing a remote Elastic Node v5.
 */
public class ENv5TwinStub extends TwinStub {

    // --- Own public API --------------------------------------

    public ENv5TwinStub(String identifier) {
        super(identifier);
    }

    public void activateLED(int ledNumber) {
        String led = LED(ledNumber);
        publishOnCommand(led);
    }

    public void deactivateLED(int ledNumber) {
        String led = LED(ledNumber);
        publishOffCommand(led);
    }

    private String LED(int number) {
        return "led" + number;
    }

}
