package de.ude.ies.elastic_ai.comm;

import de.ude.ies.elastic_ai.protocol.BrokerStub;
import de.ude.ies.elastic_ai.protocol.Posting;
import de.ude.ies.elastic_ai.protocol.Subscriber;
import java.util.*;
import java.util.stream.Collectors;

public class BrokerMock implements BrokerStub {

    private boolean connected = false;

    private record Subscription(List<String> topicFilter, Subscriber subscriber) {
        public Subscription(String topicFilter, Subscriber subscriber) {
            this(getTokensWithCollection(topicFilter), subscriber);
        }

        public boolean matches(String msgTopic) {
            return new Matcher(getTokensWithCollection(msgTopic), topicFilter).check();
        }

        private static List<String> getTokensWithCollection(String str) {
            return Collections.list(new StringTokenizer(str, "/"))
                .stream()
                .map(token -> (String) token)
                .collect(Collectors.toList());
        }
    }

    private static class Matcher {

        private final Iterator<String> msgTokens;
        private final Iterator<String> filterTokens;
        private boolean isMatching;

        public Matcher(List<String> msgTokenList, List<String> filterTokenList) {
            msgTokens = msgTokenList.iterator();
            filterTokens = filterTokenList.iterator();
        }

        public boolean check() {
            while (hasMoreTokensToCheck()) {
                boolean isDone = checkToken(msgTokens.next(), filterTokens.next());
                if (isDone) return isMatching;
            }
            return allTokensConsumed();
        }

        private boolean hasMoreTokensToCheck() {
            return msgTokens.hasNext() && filterTokens.hasNext();
        }

        private boolean checkToken(String msgToken, String filterToken) {
            return switch (filterToken) {
                case "+" -> singleLevelWildcard();
                case "#" -> multiLevelWildcard();
                default -> noWildcard(msgToken, filterToken);
            };
        }

        private boolean noWildcard(String msgToken, String filterToken) {
            if (!msgToken.equals(filterToken)) {
                isMatching = false;
                return true;
            }
            return false;
        }

        private boolean singleLevelWildcard() {
            return false;
        }

        private boolean multiLevelWildcard() {
            isMatching = true;
            return true;
        }

        private boolean allTokensConsumed() {
            return !msgTokens.hasNext() && !filterTokens.hasNext();
        }
    }

    private final List<Subscription> subscriptions = new ArrayList<>();
    private final String identifier;

    //    private final String clientID;

    public BrokerMock(String identifier) {
        this.identifier = fixIdentifier(identifier);
    }

    private String fixIdentifier(String id) {
        if (id.startsWith("/")) {
            id = id.substring(1);
        }
        if (id.endsWith("/")) {
            id = id.substring(0, id.length() - 1);
        }
        return id.strip();
    }

    @Override
    public void subscribe(String topic, Subscriber subscriber) {
        subscribeRaw(identifier + "/" + topic, subscriber);
    }

    @Override
    public void subscribeRaw(String topic, Subscriber subscriber) {
        var subscription = new Subscription(topic, subscriber);
        subscriptions.add(subscription);
        System.out.println("Subscribed to: " + topic);
    }

    @Override
    public void unsubscribe(String topic) {
        unsubscribeRaw(identifier + "/" + topic);
    }

    @Override
    public void unsubscribeRaw(String topic) {
        subscriptions.removeIf(sub -> sub.matches(topic));
        System.out.println("Unsubscribed from: " + topic);
    }

    @Override
    public String getClientIdentifier() {
        return identifier;
    }

    @Override
    public String getDomain() {
        return identifier;
    }

    @Override
    public void connect(String clientId, String lwtMessage) {
        connected = true;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void publish(Posting posting, boolean retain) {
        Posting toPublish = new Posting(identifier + "/" + posting.topic(), posting.data());
        executePublish(toPublish);
        System.out.println("Published to: " + toPublish.topic() + ", Message: " + toPublish.data());
    }

    private Posting rewriteTopicToIncludeMe(Posting posting) {
        return posting.cloneWithTopicAffix(identifier);
    }

    private void executePublish(Posting toPublish) {
        for (Subscription sub : new ArrayList<>(subscriptions)) {
            if (sub.matches(toPublish.topic())) {
                try {
                    sub.subscriber().deliver(toPublish);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void deliverIfTopicMatches(Posting msg, Subscription subscription) {
        if (subscription.matches(msg.topic())) {
            try {
                subscription.subscriber().deliver(msg);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
