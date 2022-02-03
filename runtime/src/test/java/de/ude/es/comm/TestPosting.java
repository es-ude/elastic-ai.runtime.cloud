package de.ude.es.comm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPosting {

    @Test
    void startPostingCanBeDetected() {
        Posting p = Posting.createStartSending("/temp", "receiver");
        assertTrue(p.isStartSending("/temp"));
    }
}
