package de.ude.ies.elastic_ai.Clients;

import static com.google.common.primitives.UnsignedInteger.ONE;
import static de.ude.ies.elastic_ai.protocol.Status.extractFromStatus;

import com.google.common.primitives.UnsignedInteger;
import de.ude.ies.elastic_ai.protocol.BrokerStub;
import java.util.*;
import lombok.Getter;

public class ClientList {

    @Getter
    private final List<ClientData> clients;

    private UnsignedInteger clientIdCounter = ONE;

    public ClientList() {
        clients = new ArrayList<>();
    }

    public void changeClientName(String ID, String newName) throws NullPointerException {
        ClientData client = getClient(ID);
        if (client != null) {
            client.setName(newName);
        } else {
            throw new NullPointerException("Client not found!");
        }
    }

    public ClientData getClient(String ID) {
        for (ClientData client : clients) {
            if (Objects.equals(client.getID(), ID)) {
                return client;
            }
        }
        return null;
    }

    /**
     * if client already exists -> sets client.active=true,
     * else -> adds new client.
     */
    public void addOrUpdateClient(String ID, String posting, BrokerStub endpoint) {
        if (getClient(ID) == null) {
            String type = extractFromStatus(posting, "TYPE");
            // if monitor
            if (type != null && type.equals("MONITOR")) {
                return;
            }

            clients.add(new ClientData("Client " + clientIdCounter, ID, endpoint));
            clientIdCounter = clientIdCounter.plus(ONE);
        }
        getClient(ID).updateValues(posting);
    }
}
