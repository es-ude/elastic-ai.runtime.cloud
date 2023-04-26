package org.ude.es.twinImplementations;

import org.ude.es.twinBase.ExecutableJavaTwin;
import org.ude.es.twinBase.TwinStub;

public class minimalDigitalTwin extends ExecutableJavaTwin {

    private final TwinStub twinStub;

    public static void main(String[] args) throws InterruptedException {
        startJavaTwin(args, "example");
    }

    public minimalDigitalTwin(String identifier) {
        super(identifier + "Twin");
        twinStub = new TwinStub(identifier);
    }
}
