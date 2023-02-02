package org.ude.es.comm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestPosting {

    private final String TOPIC = "/top";
    private final String PAYLOAD = "load";

    //TODO: write rest of tests!

    @Test
    void testCreateCommand() {
        Posting posting = Posting.createCommand(TOPIC, PAYLOAD);
        assertEquals("/SET" + TOPIC, posting.topic());
        assertEquals(PAYLOAD, posting.data());
    }

    @Test
    void testStartSending() {
        Posting posting = Posting.createStartSending(TOPIC, PAYLOAD);
        assertEquals("/START" + TOPIC, posting.topic());
        assertEquals(PAYLOAD, posting.data());
    }

    @Test
    void testStopSending() {
        Posting posting = Posting.createStopSending(TOPIC, PAYLOAD);
        assertEquals("/STOP" + TOPIC, posting.topic());
        assertEquals(PAYLOAD, posting.data());
    }

    @Test
    void testDataPosting() {
        Posting posting = Posting.createData(TOPIC, PAYLOAD);
        assertEquals("/DATA" + TOPIC, posting.topic());
        assertEquals(PAYLOAD, posting.data());
    }

    @Test
    void testStatusOnlinePosting() {
        Posting posting = Posting.createStatus(PAYLOAD, true);
        assertEquals("/STATUS", posting.topic());
        assertEquals(PAYLOAD + ";TWIN;1", posting.data());
    }

    @Test
    void testStatusOfflinePosting() {
        Posting posting = Posting.createStatus(PAYLOAD, false);
        assertEquals("/STATUS", posting.topic());
        assertEquals(PAYLOAD + ";TWIN;0", posting.data());
    }

    @Test
    void testCloneWithTopicAffix() {
        Posting posting = Posting.createData(TOPIC, PAYLOAD);
        posting = posting.cloneWithTopicAffix("affix");
        assertEquals("affix/DATA" + TOPIC, posting.topic());
        assertEquals(PAYLOAD, posting.data());
    }
}
