package de.ude.es;

import de.ude.es.comm.HivemqBroker;

public class Main {

  private static final String DOMAIN = "eip://uni-due.de/es";
  private static final String IP = "localhost";
  private static final int PORT = 1883;
  private static HivemqBroker broker;

  public static void main(String[] args) {
    broker = new HivemqBroker(DOMAIN, IP, PORT);
    // Your code here
  }
}
