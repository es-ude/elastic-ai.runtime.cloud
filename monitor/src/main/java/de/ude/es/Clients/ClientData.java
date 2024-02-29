package de.ude.es.Clients;

import java.util.HashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;
import org.ude.es.protocol.BrokerStub;
import org.ude.es.protocol.requests.DataRequester;

public class ClientData {

    private final String ID;

    @Getter
    private final RemoteCommunicationEndpoint remoteCommunicationEndpoint;

    @Setter
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

    public ClientData(
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

    public void setActive() {
        this.active = true;
    }

    public void setInactive() {
        this.active = false;
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
        return String.format("ClientData{ name='%s', ID='%s' }", name, ID);
    }
}
