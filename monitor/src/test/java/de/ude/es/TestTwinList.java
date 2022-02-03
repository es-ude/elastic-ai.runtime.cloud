package de.ude.es;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestTwinList {

    TwinList twinList;

    @BeforeEach
    void setUp() {
        twinList = new TwinList();

        twinList.addTwin("ID0");
        twinList.addTwin("ID1");
        twinList.addTwin("ID2");
    }

    @Test
    void testAddTwin() {
        List<TwinData> expected = List.of(
                new TwinData("Twin 0", "ID0"),
                new TwinData("Twin 1", "ID1"),
                new TwinData("Twin 2", "ID2")
        );

        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());

        twinList.addTwin("ID3");
        expected = List.of(
                new TwinData("Twin 0", "ID0"),
                new TwinData("Twin 1", "ID1"),
                new TwinData("Twin 2", "ID2"),
                new TwinData("Twin 3", "ID3")
        );

        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());
    }

    @Test
    void testAddTwinDuplicate() {
        List<TwinData> expected = List.of(
                new TwinData("Twin 0", "ID0"),
                new TwinData("Twin 1", "ID1"),
                new TwinData("Twin 2", "ID2")
        );

        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());

        twinList.addTwin("ID1");
        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());
    }

    @Test
    void testChangeTwinName() {
        twinList.changeTwinName("ID0", "Twin_0_new_name");
        twinList.changeTwinName("ID2", "Twin_2_new_name");
        twinList.changeTwinName("ID", "new_name");

        List<TwinData> expected = List.of(
                new TwinData("Twin_0_new_name", "ID0"),
                new TwinData("Twin 1", "ID1"),
                new TwinData("Twin_2_new_name", "ID2")
        );

        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());
    }

    @Test
    void testGetTwin() {
        assertEquals(new TwinData("Twin 1", "ID1").toString(), twinList.getTwin("ID1").toString());
        assertNull(twinList.getTwin("ID"));
    }

}
