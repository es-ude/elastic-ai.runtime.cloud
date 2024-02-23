package de.ude.es;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.protocol.BrokerStub;

public class TestClientList {

    ClientList clientList;
    BrokerStub brokerStubMock = new BrokerStubMock() {};

    @BeforeEach
    void setUp() {
        clientList = new ClientList();

        clientList.addOrUpdateClient(
            "ID1",
            new String[] {},
            brokerStubMock,
            "requesterID"
        );
        clientList.addOrUpdateClient(
            "ID2",
            new String[] {},
            brokerStubMock,
            "requesterID"
        );
        clientList.addOrUpdateClient(
            "ID3",
            new String[] {},
            brokerStubMock,
            "requesterID"
        );
    }

    @Test
    void testChangeNameThrowsNullPointerExceptionIfClientDoesNotExist() {
        assertThrows(
            NullPointerException.class,
            () -> clientList.changeClientName("WRONG_ID", "NewName")
        );
    }

    @Test
    void testChangeClientNameSuccessful() {
        clientList.changeClientName("ID1", "Client_1_new_name");
        clientList.changeClientName("ID3", "Client_3_new_name");

        List<ClientData> expected = List.of(
            new ClientData(
                "Client_1_new_name",
                "ID1",
                brokerStubMock,
                "requesterID"
            ),
            new ClientData("Client 2", "ID2", brokerStubMock, "requesterID"),
            new ClientData(
                "Client_3_new_name",
                "ID3",
                brokerStubMock,
                "requesterID"
            )
        );

        assertEquals(expected.toString(), clientList.getClients().toString());
        assertEquals(expected.size(), clientList.getClients().size());
    }

    @Test
    void testGetClients() {
        List<ClientData> Clients = List.of(
            new ClientData("Client 1", "ID1", brokerStubMock, "requesterID"),
            new ClientData("Client 2", "ID2", brokerStubMock, "requesterID"),
            new ClientData("Client 3", "ID3", brokerStubMock, "requesterID")
        );

        assertEquals(Clients.toString(), clientList.getClients().toString());
    }

    @Test
    void testGetActiveClients() {
        List<ClientData> expected = List.of(
            new ClientData("Client 1", "ID1", brokerStubMock, "requesterID"),
            new ClientData("Client 3", "ID3", brokerStubMock, "requesterID")
        );
        clientList.getClient("ID2").setInactive();

        assertEquals(expected.toString(), clientList.getActiveClients().toString());
    }

    @Test
    void testGetClient() {
        assertEquals(
            new ClientData("Client 2", "ID2", brokerStubMock, "requesterID")
                .toString(),
            clientList.getClient("ID2").toString()
        );
        assertNull(clientList.getClient("WRONG_ID"));
    }

    @Test
    void testAddClient() {
        List<ClientData> expected = List.of(
            new ClientData("Client 1", "ID1", brokerStubMock, "requesterID"),
            new ClientData("Client 2", "ID2", brokerStubMock, "requesterID"),
            new ClientData("Client 3", "ID3", brokerStubMock, "requesterID")
        );

        assertEquals(expected.toString(), clientList.getClients().toString());
        assertEquals(expected.size(), clientList.getClients().size());

        clientList.addOrUpdateClient(
            "ID4",
            new String[] {},
            brokerStubMock,
            "requesterID"
        );
        expected =
            List.of(
                new ClientData("Client 1", "ID1", brokerStubMock, "requesterID"),
                new ClientData("Client 2", "ID2", brokerStubMock, "requesterID"),
                new ClientData("Client 3", "ID3", brokerStubMock, "requesterID"),
                new ClientData("Client 4", "ID4", brokerStubMock, "requesterID")
            );

        assertEquals(expected.toString(), clientList.getClients().toString());
        assertEquals(expected.size(), clientList.getClients().size());
    }

    @Test
    void testAddClientDuplicate() {
        List<ClientData> expected = List.of(
            new ClientData("Client 1", "ID1", brokerStubMock, "requesterID"),
            new ClientData("Client 2", "ID2", brokerStubMock, "requesterID"),
            new ClientData("Client 3", "ID3", brokerStubMock, "requesterID")
        );

        assertEquals(expected.toString(), clientList.getClients().toString());
        assertEquals(expected.size(), clientList.getClients().size());

        clientList.addOrUpdateClient(
            "ID2",
            new String[] {},
            brokerStubMock,
            "requesterID"
        );
        assertEquals(expected.toString(), clientList.getClients().toString());
        assertEquals(expected.size(), clientList.getClients().size());
    }

    @Test
    void testAddClientDuplicateSetsClientActive() {
        List<ClientData> expected = List.of(
            new ClientData("Client 1", "ID1", brokerStubMock, "requesterID"),
            new ClientData("Client 2", "ID2", brokerStubMock, "requesterID"),
            new ClientData("Client 3", "ID3", brokerStubMock, "requesterID")
        );
        clientList.getClient("ID2").setInactive();
        assertEquals(expected.toString(), clientList.getClients().toString());
        assertEquals(expected.size(), clientList.getClients().size());
        assertEquals(expected.size() - 1, clientList.getActiveClients().size());

        clientList.addOrUpdateClient(
            "ID2",
            new String[] {},
            brokerStubMock,
            "requesterID"
        );
        assertEquals(expected.toString(), clientList.getClients().toString());
        assertEquals(expected.size(), clientList.getActiveClients().size());
    }
}
