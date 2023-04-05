package de.ude.es;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestTwinList {

    TwinList twinList;

    @BeforeEach
    void setUp() {
        twinList = new TwinList();

        twinList.addOrUpdateTwin("ID1", new String[]{});
        twinList.addOrUpdateTwin("ID2", new String[]{});
        twinList.addOrUpdateTwin("ID3", new String[]{});
    }

    @Test
    void testChangeNameThrowsNullPointerExceptionIfTwinDoesNotExist() {
        assertThrows(
            NullPointerException.class,
            () -> twinList.changeTwinName("WRONG_ID", "NewName")
        );
    }

    @Test
    void testChangeTwinNameSuccessful() {
        twinList.changeTwinName("ID1", "Twin_1_new_name");
        twinList.changeTwinName("ID3", "Twin_3_new_name");

        List<TwinData> expected = List.of(
            new TwinData("Twin_1_new_name", "ID1"),
            new TwinData("Twin 2", "ID2"),
            new TwinData("Twin_3_new_name", "ID3")
        );

        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());
    }

    @Test
    void testGetTwins() {
        List<TwinData> twins = List.of(
            new TwinData("Twin 1", "ID1"),
            new TwinData("Twin 2", "ID2"),
            new TwinData("Twin 3", "ID3")
        );

        assertEquals(twins.toString(), twinList.getTwins().toString());
    }

    @Test
    void testGetActiveTwins() {
        List<TwinData> expected = List.of(
            new TwinData("Twin 1", "ID1"),
            new TwinData("Twin 3", "ID3")
        );
        twinList.getTwin("ID2").setInactive();

        assertEquals(expected.toString(), twinList.getActiveTwins().toString());
    }

    @Test
    void testGetTwin() {
        assertEquals(
            new TwinData("Twin 2", "ID2").toString(),
            twinList.getTwin("ID2").toString()
        );
        assertNull(twinList.getTwin("WRONG_ID"));
    }

    @Test
    void testAddTwin() {
        List<TwinData> expected = List.of(
            new TwinData("Twin 1", "ID1"),
            new TwinData("Twin 2", "ID2"),
            new TwinData("Twin 3", "ID3")
        );

        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());

        twinList.addOrUpdateTwin("ID4", new String[]{});
        expected =
            List.of(
                new TwinData("Twin 1", "ID1"),
                new TwinData("Twin 2", "ID2"),
                new TwinData("Twin 3", "ID3"),
                new TwinData("Twin 4", "ID4")
            );

        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());
    }

    @Test
    void testAddTwinDuplicate() {
        List<TwinData> expected = List.of(
            new TwinData("Twin 1", "ID1"),
            new TwinData("Twin 2", "ID2"),
            new TwinData("Twin 3", "ID3")
        );

        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());

        twinList.addOrUpdateTwin("ID2", new String[]{});
        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());
    }

    @Test
    void testAddTwinDuplicateSetsTwinActive() {
        List<TwinData> expected = List.of(
            new TwinData("Twin 1", "ID1"),
            new TwinData("Twin 2", "ID2"),
            new TwinData("Twin 3", "ID3")
        );
        twinList.getTwin("ID2").setInactive();
        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());
        assertEquals(expected.size() - 1, twinList.getActiveTwins().size());

        twinList.addOrUpdateTwin("ID2", new String[]{});
        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getActiveTwins().size());
    }
}
