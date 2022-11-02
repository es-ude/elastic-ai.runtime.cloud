package de.ude.es;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestTwinData {

    TwinData twinData;

    @BeforeEach
    void setUp() {
        twinData = new TwinData("Name", "ID");
    }

    @Test
    void testGetName() {
        assertEquals("Name", twinData.getName());
    }

    @Test
    void testActive() {
        assertTrue(twinData.isActive());

        twinData.setNotActive();
        assertFalse(twinData.isActive());

        twinData.setActive();
        assertTrue(twinData.isActive());
    }

    @Test
    void testSetName() {
        TwinData twinDataCompare = new TwinData("NewName", "ID");
        twinData.setName("NewName");
        assertEquals(twinDataCompare.toString(), twinData.toString());
    }

    @Test
    void testGetID() {
        assertEquals("ID", twinData.getID());
    }
}
