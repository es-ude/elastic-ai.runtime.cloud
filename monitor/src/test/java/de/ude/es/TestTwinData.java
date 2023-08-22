package de.ude.es;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.comm.CommunicationEndpoint;

public class TestTwinData {

    private final String TWIN_NAME = "NAME";
    private final String TWIN_ID = "ID";
    TwinData twinData;

    @BeforeEach
    void setUp() {
        CommunicationEndpoint CommunicationEndpointMock =
            new CommunicationEndpointMock() {};
        twinData =
        new TwinData(
            TWIN_NAME,
            TWIN_ID,
            CommunicationEndpointMock,
            "requesterID"
        );
    }

    @Test
    void testGetName() {
        assertEquals(TWIN_NAME, twinData.getName());
    }

    @Test
    void testSetName() {
        twinData.setName("NewName");
        assertEquals("NewName", twinData.getName());
    }

    @Test
    void testIsActive() {
        assertTrue(twinData.isActive());
    }

    @Test
    void testSetInactive() {
        twinData.setInactive();
        assertFalse(twinData.isActive());
    }

    @Test
    void testSetActive() {
        twinData.setInactive();
        twinData.setActive();
        assertTrue(twinData.isActive());
    }

    @Test
    void testGetId() {
        assertEquals(TWIN_ID, twinData.getId());
    }

    @Test
    void testActive() {
        assertTrue(twinData.isActive());

        twinData.setInactive();
        assertFalse(twinData.isActive());

        twinData.setActive();
        assertTrue(twinData.isActive());
    }

    @Test
    void testGetID() {
        assertEquals("ID", twinData.getId());
    }

    @Test
    void testToString() {
        String expectedResult =
            "TwinData{ name='" + TWIN_NAME + "', ID='" + TWIN_ID + "' }";
        assertEquals(expectedResult, twinData.toString());
    }
}
