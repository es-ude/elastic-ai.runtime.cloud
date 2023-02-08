package org.ude.es.protocol;


import org.ude.es.twinBase.TwinStub;

class RequesterTwinStub extends TwinStub {

    public int subNum;

    public RequesterTwinStub(String identifier) {
        super(identifier);
        this.subNum = 1;
    }

    public void newSubscriber() {
        this.subNum++;
    }

    public void subscriberLeaves() {
        this.subNum--;
    }

    public boolean hasSubscriber() {
        return this.subNum != 0;
    }
}
