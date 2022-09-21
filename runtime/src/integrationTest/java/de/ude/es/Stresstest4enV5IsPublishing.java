package de.ude.es;

import de.ude.es.comm.HivemqBroker;
import de.ude.es.exampleTwins.IntegrationTestTwinForEnV5;
import de.ude.es.twin.JavaTwin;
import de.ude.es.twin.TwinStub;

public class Stresstest4enV5IsPublishing {

  private static final String DOMAIN = "eip://uni-due.de/es";
  private static final String IP = "localhost";
  private static final int PORT = 1883;

  public static void main(String[] args) {
    HivemqBroker broker = new HivemqBroker(DOMAIN, IP, PORT);
    IntegrationTestTwinForEnV5 twin = new IntegrationTestTwinForEnV5(
      "integTestTwin"
    );
    twin.bind(broker);
    twin.startSubscribing("stresstestPub");
  }
}
