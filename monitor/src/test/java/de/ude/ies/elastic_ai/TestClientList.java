package de.ude.ies.elastic_ai;

import static org.junit.jupiter.api.Assertions.*;

import de.ude.ies.elastic_ai.Clients.ClientData;
import de.ude.ies.elastic_ai.Clients.ClientList;
import de.ude.ies.elastic_ai.protocol.BrokerStub;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestClientList {

    ClientList clientList;
    BrokerStub brokerStubMock = new BrokerStubMock() {};

    @BeforeEach
    void setUp() {
        clientList = new ClientList();

        clientList.addOrUpdateClient("ID1", "", brokerStubMock);
        clientList.addOrUpdateClient("ID2", "", brokerStubMock);
        clientList.addOrUpdateClient("ID3", "", brokerStubMock);
    }

    @Test
    void testChangeNameThrowsNullPointerExceptionIfClientDoesNotExist() {
        assertThrows(NullPointerException.class, () ->
            clientList.changeClientName("WRONG_ID", "NewName")
        );
    }

    @Test
    void testChangeClientNameSuccessful() {
        clientList.changeClientName("ID1", "Client_1_new_name");
        clientList.changeClientName("ID3", "Client_3_new_name");

        List<ClientData> expected = List.of(
            new ClientData("Client_1_new_name", "ID1", brokerStubMock),
            new ClientData("Client 2", "ID2", brokerStubMock),
            new ClientData("Client_3_new_name", "ID3", brokerStubMock)
        );

        assertEquals(expected.toString(), clientList.getClients().toString());
        assertEquals(expected.size(), clientList.getClients().size());
    }

    @Test
    void testGetClients() {
        List<ClientData> Clients = List.of(
            new ClientData("Client 1", "ID1", brokerStubMock),
            new ClientData("Client 2", "ID2", brokerStubMock),
            new ClientData("Client 3", "ID3", brokerStubMock)
        );

        assertEquals(Clients.toString(), clientList.getClients().toString());
    }

    @Test
    void testGetClient() {
        assertEquals(
            new ClientData("Client 2", "ID2", brokerStubMock).toString(),
            clientList.getClient("ID2").toString()
        );
        assertNull(clientList.getClient("WRONG_ID"));
    }

    @Test
    void testAddClient() {
        List<ClientData> expected = List.of(
            new ClientData("Client 1", "ID1", brokerStubMock),
            new ClientData("Client 2", "ID2", brokerStubMock),
            new ClientData("Client 3", "ID3", brokerStubMock)
        );

        assertEquals(expected.toString(), clientList.getClients().toString());
        assertEquals(expected.size(), clientList.getClients().size());

        clientList.addOrUpdateClient("ID4", "", brokerStubMock);
        expected = List.of(
            new ClientData("Client 1", "ID1", brokerStubMock),
            new ClientData("Client 2", "ID2", brokerStubMock),
            new ClientData("Client 3", "ID3", brokerStubMock),
            new ClientData("Client 4", "ID4", brokerStubMock)
        );

        assertEquals(expected.toString(), clientList.getClients().toString());
        assertEquals(expected.size(), clientList.getClients().size());
    }

    @Test
    void testAddClientDuplicate() {
        List<ClientData> expected = List.of(
            new ClientData("Client 1", "ID1", brokerStubMock),
            new ClientData("Client 2", "ID2", brokerStubMock),
            new ClientData("Client 3", "ID3", brokerStubMock)
        );

        assertEquals(expected.toString(), clientList.getClients().toString());
        assertEquals(expected.size(), clientList.getClients().size());

        clientList.addOrUpdateClient("ID2", "", brokerStubMock);
        assertEquals(expected.toString(), clientList.getClients().toString());
        assertEquals(expected.size(), clientList.getClients().size());
    }
}
