package de.ude.ies.elastic_ai.protocol;

import static de.ude.ies.elastic_ai.protocol.Status.State.ONLINE;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestStatus {

    Status status;

    @BeforeEach
    void setup() {
        status = new Status();
        status.ID("ID").STATE(ONLINE).TYPE("TYPE");
    }

    @Test
    void testMandatoryStatus() {
        Assertions.assertEquals("ID:ID;TYPE:TYPE;STATE:" + ONLINE + ";", status.get());
    }

    @Test
    void testOptionalStatus() {
        status.ADD_OPTIONAL("key0", "value0").ADD_OPTIONAL("key1", "value1");
        Assertions.assertEquals(
            "ID:ID;TYPE:TYPE;STATE:" + ONLINE + ";key0:value0;key1:value1;",
            status.get()
        );
        status.SET_OPTIONAL("key:value;");
        Assertions.assertEquals("ID:ID;TYPE:TYPE;STATE:" + ONLINE + ";key:value;", status.get());
    }

    @Test
    void testDataStatus() {
        status.ADD_DATA("data0").ADD_DATA("data1");
        Assertions.assertEquals(
            "ID:ID;TYPE:TYPE;STATE:" + ONLINE + ";DATA:data0,data1;",
            status.get()
        );
        status.SET_DATA("data");
        Assertions.assertEquals("ID:ID;TYPE:TYPE;STATE:" + ONLINE + ";DATA:data;", status.get());
    }
}
