package de.ude.es.twin;

import de.ude.es.comm.Protocol;

/**
 * A digital twin representing a remote Elastic Node v5.
 */
public class ENv5Twin extends DigitalTwin {

    private Protocol protocol;

    // --- Own public API --------------------------------------

    public ENv5Twin(String identifier) {
        super(identifier);
    }

    public void activateLED(int ledNumber) {
        String led = LED(ledNumber);
        protocol.publishOnCommand(led);
    }

    public void deactivateLED(int ledNumber) {
        String led = LED(ledNumber);
        protocol.publishOffCommand(led);
    }

    private String LED(int number) {
        return "/led" + number;
    }


    // --- Methods for DigitalTwin -----------------------------

    @Override
    protected void executeOnBind() {
        protocol = new Protocol(this);
    }


}
