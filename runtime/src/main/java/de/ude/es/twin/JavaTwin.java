package de.ude.es.twin;


import de.ude.es.comm.Posting;

public class JavaTwin extends Twin {

    public JavaTwin(String identifier) {
        super(identifier);
    }

    public void publish(Posting posting) {
        Posting toSend = posting.cloneWithTopicAffix(identifier);
        endpoint.publish(toSend);
    }

    public void publishData(String dataId, String value) {
        Posting post = Posting.createData(dataId, value);
        this.publish(post);
    }

    public void publishHeartbeat(String who) {
        Posting post = Posting.createHeartbeat(who);
        this.publish(post);
    }

    public String ID() {
        return endpoint.ID() + identifier;
    }

}
