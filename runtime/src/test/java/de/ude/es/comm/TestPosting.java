package de.ude.es.comm;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TestPosting {

  @Test
  void startPostingCanBeDetected() {
    Posting p = Posting.createStartSending("/temp", "receiver");
    assertTrue(p.isStartSending("/temp"));
  }
}
