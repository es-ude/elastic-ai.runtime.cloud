package de.ude.ies.elastic_ai.protocol;

import java.util.Objects;

/**
 * A helper class that encapsulates knowledge about how to format the different
 * posting types as strings in a topic.
 */
public enum PostingType {
    DATA("/DATA"),
    START("/START"),
    STOP("/STOP"),
    COMMAND("/DO"),
    DONE("/DONE"),
    STATUS("/STATUS");

    private final String value;

    PostingType(String s) {
        value = s;
    }

    public String topic(String topicID) {
        if (Objects.equals(topicID, "")) {
            return value;
        }
        return value + fixTopicId(topicID);
    }

    private String fixTopicId(String topicId) {
        if (!topicId.startsWith("/")) {
            topicId = "/" + topicId;
        }
        if (topicId.endsWith("/")) {
            topicId = topicId.substring(0, topicId.length() - 1);
        }
        return topicId.strip();
    }
}
