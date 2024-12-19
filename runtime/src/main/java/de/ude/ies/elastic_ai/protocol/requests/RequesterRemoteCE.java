package de.ude.ies.elastic_ai.protocol.requests;

import de.ude.ies.elastic_ai.communicationEndpoints.RemoteCommunicationEndpoint;

class RequesterRemoteCE extends RemoteCommunicationEndpoint {

    public int subNum;

    public RequesterRemoteCE(String identifier) {
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
