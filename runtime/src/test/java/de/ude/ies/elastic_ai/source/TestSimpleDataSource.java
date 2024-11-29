package de.ude.ies.elastic_ai.source;

import de.ude.ies.elastic_ai.Checker;
import de.ude.ies.elastic_ai.protocol.Posting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
