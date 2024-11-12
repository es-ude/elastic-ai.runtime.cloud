package de.ude.ies.elastic_ai;

import static org.junit.jupiter.api.Assertions.*;

import de.ude.ies.elastic_ai.Clients.ClientData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestClientData {

    private final String CLIENT_NAME = "NAME";
    private final String CLIENT_ID = "ID";
    private final String CLIENT_TYPE = "TYPE";
    private final String CLIENT_ACTIVE = "ONLINE";
    private final String CLIENT_DATA = "data1,data2";
    private final String CLIENT_OPTIONAL = "OPT1:opt1;OPT2:opt2";
    private final HashMap<String, String> CLIENT_OPTIONAL_VALUES = new HashMap<>();
    private final HashMap<String, String> CLIENT_LAST_DATA_VALUES = new HashMap<>();
    ClientData clientData;

    @BeforeEach
    void setUp() {
        BrokerStubMock brokerMock = new BrokerStubMock() {};
        clientData = new ClientData(CLIENT_NAME, CLIENT_ID, brokerMock);
        CLIENT_LAST_DATA_VALUES.put("data1", "");
        CLIENT_LAST_DATA_VALUES.put("data2", "");
        CLIENT_OPTIONAL_VALUES.put("OPT1", "opt1");
        CLIENT_OPTIONAL_VALUES.put("OPT2", "opt2");
        clientData.updateValues(
            "TYPE:" +
            CLIENT_TYPE +
            ";STATE:" +
            CLIENT_ACTIVE +
            ";DATA:" +
            CLIENT_DATA +
            ";" +
            CLIENT_OPTIONAL +
            ";"
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
        assertEquals(CLIENT_ID, clientData.getID());
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
        assertEquals("ID", clientData.getID());
    }

    @Test
    void testToString() {
        String expectedResult =
            "ClientData{" +
            "name='" +
            CLIENT_NAME +
            '\'' +
            ", ID='" +
            CLIENT_ID +
            '\'' +
            ", type='" +
            CLIENT_TYPE +
            '\'' +
            ", active=" +
            Objects.equals(CLIENT_ACTIVE, "ONLINE") +
            ", dataValues=" +
            Arrays.toString(CLIENT_DATA.split(",")) +
            ", optionalValues=" +
            CLIENT_OPTIONAL_VALUES +
            ", dataValue=" +
            CLIENT_LAST_DATA_VALUES +
            '}';
        assertEquals(expectedResult, clientData.toString());
    }
}
