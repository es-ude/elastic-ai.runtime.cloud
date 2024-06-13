package org.ude.es.Clients;

import static org.ude.es.protocol.Status.State.ONLINE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.ude.es.communicationEndpoints.RemoteCommunicationEndpoint;
import org.ude.es.protocol.BrokerStub;
import org.ude.es.protocol.Status;
import org.ude.es.protocol.requests.DataRequester;

public class ClientData {

    @Setter
    @Getter
    private String name;

    @Getter
    private final String ID;

    @Getter
    private String type;

    @Getter
    private boolean active;

    private String[] dataValues;

    @Getter
    private final RemoteCommunicationEndpoint remoteCommunicationEndpoint;

    @Getter
    private final HashMap<String, DataRequester> dataRequester;

    @Getter
    private final HashMap<String, String> lastDataValues;

    private final HashMap<String, String> optionalValues;

    public ClientData(String name, String ID, BrokerStub endpoint) {
        this.name = name;
        this.ID = ID;

        remoteCommunicationEndpoint = new RemoteCommunicationEndpoint(ID);
        remoteCommunicationEndpoint.bindToCommunicationEndpoint(endpoint);

        dataRequester = new HashMap<>();
        lastDataValues = new HashMap<>();
        optionalValues = new HashMap<>();

        this.dataValues = new String[0];
    }

    public void setActive() {
        this.active = true;
    }

    public void setInactive() {
        this.active = false;
    }

    public List<String> getAvailableData() {
        return List.of(dataValues);
    }

    public String getOptionalStatusValue(String key) {
        return optionalValues.get(key);
    }

    public List<String> getAvailableOptionalStatus() {
        return optionalValues.keySet().stream().toList();
    }

    public void setAvailableData(String[] sensors) {
        this.dataValues = sensors;
        dataRequester.clear();
        for (String sensor : sensors) {
            DataRequester requester = new DataRequester(
                this.remoteCommunicationEndpoint,
                sensor,
                "monitor"
            );
            requester.listenToData(true);
            requester.setDataReceiveFunction(
                data -> lastDataValues.put(sensor, data)
            );
            dataRequester.put(sensor, requester);
            lastDataValues.put(sensor, "");
        }
    }

    public void updateValues(String posting) {
        optionalValues.clear();
        setAvailableData(new String[] {});
        for (String post : posting.split(";")) {
            if (post.split(":").length < 2) continue;
            String key = post.split(":")[0];
            String value = Status.extractFromStatus(posting, key);
            if (Objects.equals(key, "TYPE")) {
                type = value;
            } else if (Objects.equals(key, "STATE")) {
                active = Objects.equals(value, ONLINE.toString());
            } else if (Objects.equals(key, "DATA")) {
                assert value != null;
                setAvailableData(value.split(","));
            } else if (!Objects.equals(key, "ID")) {
                optionalValues.put(key, value);
            }
        }
    }

    @Override
    public String toString() {
        return (
            "ClientData{" +
            "name='" +
            name +
            '\'' +
            ", ID='" +
            ID +
            '\'' +
            ", type='" +
            type +
            '\'' +
            ", active=" +
            active +
            ", dataValues=" +
            Arrays.toString(dataValues) +
            ", optionalValues=" +
            optionalValues +
            ", dataValue=" +
            lastDataValues +
            '}'
        );
    }
}
