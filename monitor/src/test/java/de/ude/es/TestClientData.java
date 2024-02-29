package de.ude.es;

import static org.junit.jupiter.api.Assertions.*;

import de.ude.es.Clients.ClientData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.protocol.BrokerStub;

public class TestClientData {

    private final String CLIENT_NAME = "NAME";
    private final String CLIENT_ID = "ID";
    ClientData clientData;

    @BeforeEach
    void setUp() {
        BrokerStubMock brokerMock = new BrokerStubMock() {};
        clientData = new ClientData(
            CLIENT_NAME,
            CLIENT_ID,
            brokerMock,
            "requesterID"
        );
        BrokerStub brokerStubMock = new BrokerStubMock() {};
        clientData = new ClientData(
            CLIENT_NAME,
            CLIENT_ID,
            brokerStubMock,
            "requesterID"
        );
    }

    @Test
    void testGetName() {
        assertEquals(CLIENT_NAME, clientData.getName());
    }

    @Test
    void testSetName() {
        clientData.setName("NewName");
        assertEquals("NewName", clientData.getName());
    }

    @Test
    void testIsActive() {
        assertTrue(clientData.isActive());
    }

    @Test
    void testSetInactive() {
        clientData.setInactive();
        assertFalse(clientData.isActive());
    }

    @Test
    void testSetActive() {
        clientData.setInactive();
        clientData.setActive();
        assertTrue(clientData.isActive());
    }

    @Test
    void testGetId() {
        assertEquals(CLIENT_ID, clientData.getId());
    }

    @Test
    void testActive() {
        assertTrue(clientData.isActive());

        clientData.setInactive();
        assertFalse(clientData.isActive());

        clientData.setActive();
        assertTrue(clientData.isActive());
    }

    @Test
    void testGetID() {
        assertEquals("ID", clientData.getId());
    }

    @Test
    void testToString() {
        String expectedResult =
            "ClientData{ name='" + CLIENT_NAME + "', ID='" + CLIENT_ID + "' }";
        assertEquals(expectedResult, clientData.toString());
    }
}
