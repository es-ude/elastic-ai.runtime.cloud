package de.ude.es;

import de.ude.es.util.Timeout;
import de.ude.es.util.Timer;
import de.ude.es.util.TimerClient;

public class TimerMock implements Timer {

  private TimeoutMock timeout;

  @Override
  public Timeout register(int timeoutMillis, TimerClient client) {
    timeout = new TimeoutMock(client);
    return timeout;
  }

  public void fire() {
    timeout.fire();
  }

  public record TimeoutMock(TimerClient client) implements Timeout {
    public void fire() {
      this.client.timeout(this);
    }
    @Override
    public void restart() {}

    @Override
    public void stop() {}
  }
}
