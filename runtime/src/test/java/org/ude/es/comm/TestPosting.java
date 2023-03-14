package org.ude.es.comm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestPosting {

    private final String TOPIC = "top";
    private final String PAYLOAD = "load";

    @Test
    void testCreateCommand() {
        Posting posting = Posting.createCommand(TOPIC, PAYLOAD);
        assertEquals("/SET/" + TOPIC, posting.topic());
        assertEquals(PAYLOAD, posting.data());
    }

    @Test
    void testStartSending() {
        Posting posting = Posting.createStartSending(TOPIC, PAYLOAD);
        assertEquals("/START/" + TOPIC, posting.topic());
        assertEquals(PAYLOAD, posting.data());
    }

    @Test
    void testStopSending() {
        Posting posting = Posting.createStopSending(TOPIC, PAYLOAD);
        assertEquals("/STOP/" + TOPIC, posting.topic());
        assertEquals(PAYLOAD, posting.data());
    }

    @Test
    void testDataPosting() {
        Posting posting = Posting.createData(TOPIC, PAYLOAD);
        assertEquals("/DATA/" + TOPIC, posting.topic());
        assertEquals(PAYLOAD, posting.data());
    }

    @Test
    void testStatusOnlinePosting() {
        Posting posting = Posting.createStatus(PAYLOAD);
        assertEquals("/STATUS", posting.topic());
        assertEquals(PAYLOAD, posting.data());
    }

    @Test
    void testCloneWithTopicAffixTrailingSlash() {
        Posting posting = Posting.createData(TOPIC + "/", PAYLOAD);
        posting = posting.cloneWithTopicAffix("affix");
        assertEquals("affix/DATA/" + TOPIC, posting.topic());
        assertEquals(PAYLOAD, posting.data());
    }

    @Test
    void testCloneWithTopicAffix2FrontSlash() {
        Posting posting = Posting.createData("/" + TOPIC, PAYLOAD);
        posting = posting.cloneWithTopicAffix("affix");
        assertEquals("affix/DATA/" + TOPIC, posting.topic());
        assertEquals(PAYLOAD, posting.data());
    }
}
