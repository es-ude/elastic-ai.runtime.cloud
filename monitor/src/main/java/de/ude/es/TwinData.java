package de.ude.es;

import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;
import org.ude.es.protocol.BrokerStub;
import org.ude.es.protocol.requests.DataRequester;

public class TwinData {

    private final String ID;

    @Getter
    private final RemoteCommunicationEndpoint remoteCommunicationEndpoint;

    @Getter
    private String name;

    @Getter
    private boolean active;

    private String[] sensors;
    private final String requesterID;

    @Getter
    private HashMap<String, DataRequester> dataRequester;

    @Getter
    private final HashMap<String, Long> lifeTime;

    public TwinData(
        String name,
        String ID,
        BrokerStub endpoint,
        String requesterID
    ) {
        this.requesterID = requesterID;
        this.ID = ID;
        this.name = name;
        this.active = true;
        this.sensors = null;
        this.remoteCommunicationEndpoint = new RemoteCommunicationEndpoint(ID);
        this.lifeTime = new HashMap<>();
        remoteCommunicationEndpoint.bindToCommunicationEndpoint(endpoint);
    }

    public void stopDataRequest(String sensor) throws InterruptedException {
        lifeTime.put(sensor, System.currentTimeMillis());
        while (System.currentTimeMillis() - lifeTime.get(sensor) < 10000) {
            Thread.sleep(100);
        }
        dataRequester.get(sensor).stopRequestingData();
    }

    public void setActive() {
        this.active = true;
    }

    public void setInactive() {
        this.active = false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return ID;
    }

    public List<String> getAvailableSensors() {
        List<String> sensorList;
        try {
            sensorList = List.of(sensors);
        } catch (Exception exception) {
            sensorList = null;
        }
        return sensorList;
    }

    public void setAvailableSensors(String[] sensors) {
        this.sensors = sensors;

        dataRequester = new HashMap<>();
        for (String sensor : sensors) {
            dataRequester.put(
                sensor,
                new DataRequester(
                    this.remoteCommunicationEndpoint,
                    sensor,
                    requesterID
                )
            );
            lifeTime.put(sensor, 0L);
        }
    }

    public String toString() {
        return String.format("TwinData{ name='%s', ID='%s' }", name, ID);
    }
}
