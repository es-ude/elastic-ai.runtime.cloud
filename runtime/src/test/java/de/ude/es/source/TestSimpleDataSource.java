package de.ude.es.source;

import de.ude.es.Checker;
import de.ude.es.comm.Posting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class TestSimpleDataSource {

    private static class SimpleDataSourceChecker extends Checker {

        public DataSource<Integer> dataSource;

        public void givenDataSource() {
            dataSource = new DataSource<>("/data");
            dataSource.bind(twin);
        }

        public void whenDataIsSetTo(int value) {
            expected = new Posting(twin.ID() + "/DATA/data", "" + value);
            dataSource.set(value);
        }
    }


    private SimpleDataSourceChecker checker;


    @BeforeEach
    void init() {
        checker = new SimpleDataSourceChecker();
    }

    @Test
    void whenDataIsSetThenPostingIsSent() {

        checker.givenBroker();
        checker.givenDigitalTwin("/twin1234");
        checker.givenDataSource();
        checker.givenRawSubscriptionAtDigitalTwinFor("/#");
        checker.whenDataIsSetTo(3);
        checker.thenPostingIsDelivered();
    }

}