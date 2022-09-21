package de.ude.es;

import de.ude.es.comm.CommunicationEndpoint;
import de.ude.es.comm.Posting;
import de.ude.es.comm.Subscriber;
import de.ude.es.twin.TwinStub;

public class HeartbeatSubscriber {

  private static class DataSubscriber implements Subscriber {

    TwinList twinList;

    DataSubscriber(TwinList twinList) {
      this.twinList = twinList;
    }

    @Override
    public void deliver(Posting posting) {
      System.out.println(posting.data());
      twinList.addTwin(posting.data());
      System.out.println(twinList.getActiveTwins());
    }
  }

  private TwinStub twinStub;
  private DataSubscriber subscriber;

  public HeartbeatSubscriber(TwinList twinList) {
    this.subscriber = new DataSubscriber(twinList);
    this.twinStub = new TwinStub("+");
  }

  public void bind(CommunicationEndpoint broker) {
    twinStub.bind(broker);
    this.twinStub.subscribeForHeartbeat(subscriber);
  }
}
