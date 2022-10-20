package de.ude.es;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestTwinList {

    TwinList twinList;

    @BeforeEach
    void setUp() {
        twinList = new TwinList(10000);

        twinList.addTwin("ID0");
        twinList.addTwin("ID1");
        twinList.addTwin("ID2");
    }

    @Test
    void testChangeTwinName() {
        twinList.changeTwinName("ID0", "Twin_0_new_name");
        twinList.changeTwinName("ID2", "Twin_2_new_name");
        twinList.changeTwinName("ID", "new_name");

        List<TwinData> expected = List.of(
            new TwinData("Twin_0_new_name", "ID0", new MonitorTimerMock(), 0),
            new TwinData("Twin 1", "ID1", new MonitorTimerMock(), 0),
            new TwinData("Twin_2_new_name", "ID2", new MonitorTimerMock(), 0)
        );

        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());
    }

    @Test
    void testGetTwin() {
        assertEquals(
            new TwinData("Twin 1", "ID1", new MonitorTimerMock(), 0).toString(),
            twinList.getTwin("ID1").toString()
        );
        assertNull(twinList.getTwin("ID"));
    }

    @Test
    void testAddTwin() {
        List<TwinData> expected = List.of(
            new TwinData("Twin 0", "ID0", new MonitorTimerMock(), 0),
            new TwinData("Twin 1", "ID1", new MonitorTimerMock(), 0),
            new TwinData("Twin 2", "ID2", new MonitorTimerMock(), 0)
        );

        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());

        twinList.addTwin("ID3");
        expected =
            List.of(
                new TwinData("Twin 0", "ID0", new MonitorTimerMock(), 0),
                new TwinData("Twin 1", "ID1", new MonitorTimerMock(), 0),
                new TwinData("Twin 2", "ID2", new MonitorTimerMock(), 0),
                new TwinData("Twin 3", "ID3", new MonitorTimerMock(), 0)
            );

        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());
    }

    @Test
    void testAddTwinDuplicate() {
        List<TwinData> expected = List.of(
            new TwinData("Twin 0", "ID0", new MonitorTimerMock(), 0),
            new TwinData("Twin 1", "ID1", new MonitorTimerMock(), 0),
            new TwinData("Twin 2", "ID2", new MonitorTimerMock(), 0)
        );

        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());

        twinList.addTwin("ID1");
        assertEquals(expected.toString(), twinList.getTwins().toString());
        assertEquals(expected.size(), twinList.getTwins().size());
    }

    @Test
    void testGetActiveTwins() {
        List<TwinData> twins = new ArrayList<>();
        twins.add(new TwinData("Twin 0", "ID0", new MonitorTimerMock(), 0));
        twins.add(new TwinData("Twin 1", "ID1", new MonitorTimerMock(), 0));
        twins.add(new TwinData("Twin 2", "ID2", new MonitorTimerMock(), 0));
        twinList
            .getTwins()
            .add(
                new TwinData("Twin 3", "ID3", new MonitorTimerMock(), 0, false)
            );

        assertEquals(twins.toString(), twinList.getActiveTwins().toString());
    }

    @Test
    void testGetTwins() {
        List<TwinData> twins = new ArrayList<>();
        twins.add(new TwinData("Twin 0", "ID0", new MonitorTimerMock(), 0));
        twins.add(new TwinData("Twin 1", "ID1", new MonitorTimerMock(), 0));
        twins.add(new TwinData("Twin 2", "ID2", new MonitorTimerMock(), 0));

        assertEquals(twins.toString(), twinList.getTwins().toString());
    }
}
