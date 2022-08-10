package de.ude.es.comm;

import java.util.Objects;

/**
 * A helper class that encapsulates knowledge about how to
 * format the different posting types as strings in a topic.
 */
public enum PostingType {
    DATA("/DATA"),
    START("/START"),
    STOP("/STOP"),
    SET("/SET"),
    LOST("/LOST"),
    HEARTBEAT("/HEART");

    private final String value;

    PostingType(String s) {
        value = s;
    }

    public String topic(String topicID) {
        if (Objects.equals(topicID, ""))
            return value;
        return value + "/" + topicID;
    }

}
