package de.ude.es.comm;

/**
 * A helper class that encapsulates knowledge about how to
 * format the different posting types as strings in a topic.
 */
enum PostingType {
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

    public String topic(String next) {
        return value+next;
    }
}
