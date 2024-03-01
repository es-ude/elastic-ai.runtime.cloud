package org.ude.es.source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ude.es.Checker;
import org.ude.es.protocol.Posting;

class TestSimpleDataSource {

    private SimpleDataSourceChecker checker;

    @BeforeEach
    void init() {
        checker = new SimpleDataSourceChecker();
    }

    @Test
    void whenDataIsSetThenPostingIsSent() {
        checker.givenBroker();
        checker.givenLocalEndpoint("twin1234");
        checker.givenDataSource();
        checker.givenSubscriptionAtLocalEndpoint("/#");
        checker.whenDataIsSetTo(3);
        checker.thenPostingIsDelivered();
    }

    private static class SimpleDataSourceChecker extends Checker {

        public DataSource<Integer> dataSource;

        public void givenDataSource() {
            dataSource = new DataSource<>("data");
            dataSource.bind(localEndpoint);
        }

        public void whenDataIsSetTo(int value) {
            isExpecting(
                new Posting(
                    localEndpoint.getDomainAndIdentifier() + "/DATA/data",
                    String.valueOf(value)
                )
            );
            dataSource.set(value);
        }
    }
}
