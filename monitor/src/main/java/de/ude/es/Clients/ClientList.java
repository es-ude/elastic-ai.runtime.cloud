package de.ude.es.Clients;

import static com.google.common.primitives.UnsignedInteger.ONE;

import com.google.common.primitives.UnsignedInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Getter;
import org.ude.es.protocol.BrokerStub;

public class ClientList {

    @Getter
    private final List<ClientData> clients;
    private UnsignedInteger clientIdCounter = ONE;

    public ClientList() {
        clients = new ArrayList<>();
    }

    public void changeClientName(String ID, String newName)
        throws NullPointerException {
        ClientData client = getClient(ID);
        if (client != null) {
            client.setName(newName);
        } else {
            throw new NullPointerException("Client not found!");
        }
    }

    public ClientData getClient(String ID) {
        for (ClientData client : clients) {
            if (Objects.equals(client.getId(), ID)) {
                return client;
            }
        }
        return null;
    }

    /**
     * if client already exists -> sets client.active=true,
     * else -> adds new client.
     */
    public void addOrUpdateClient(
        String ID,
        String[] measurements,
        BrokerStub endpoint,
        String requesterID
    ) {
        if (getClient(ID) == null) {
            clients.add(
                new ClientData(
                    "Client " + clientIdCounter.intValue(),
                    ID,
                    endpoint,
                    requesterID
                )
            );
            clientIdCounter = clientIdCounter.plus(ONE);
        } else {
            getClient(ID).setActive();
        }
        if (measurements != null) getClient(ID).setAvailableSensors(measurements);
    }

    public List<ClientData> getActiveClients() {
        List<ClientData> activeClients = new ArrayList<>();
        for (ClientData client : clients) {
            if (client.isActive()) {
                activeClients.add(client);
            }
        }
        return activeClients;
    }
}
