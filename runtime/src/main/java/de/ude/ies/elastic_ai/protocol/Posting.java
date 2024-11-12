package de.ude.ies.elastic_ai.protocol;

public record Posting(String topic, String data) {
    public Posting(String topic) {
        this(topic, "");
    }

    public static Posting createCommand(String command, String value) {
        return new Posting(PostingType.COMMAND.topic(command), value);
    }

    public static Posting createStartSending(String dataId, String receiver) {
        return new Posting(PostingType.START.topic(dataId), receiver);
    }

    public static Posting createStopSending(String dataId, String receiver) {
        return new Posting(PostingType.STOP.topic(dataId), receiver);
    }

    public static Posting createData(String phenomena, String value) {
        return new Posting(PostingType.DATA.topic(phenomena), value);
    }

    public static Posting createDone(String command, String value) {
        return new Posting(PostingType.DONE.topic(command), value);
    }

    public static Posting createStatus(String message) {
        return new Posting(PostingType.STATUS.topic(""), message);
    }

    public Posting cloneWithTopicAffix(String affix) {
        return new Posting(affix + topic(), data());
    }
}
