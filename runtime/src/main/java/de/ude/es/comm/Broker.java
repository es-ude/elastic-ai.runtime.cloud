package de.ude.es.comm;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A broker is the central communication center for a deployment.
 * It is responsible for managing subscriptions and forwarding
 * postings to the correct clients.
 * It is also responsible for forwarding to the correct subscribers
 * locally on a device!
 * Note that a broker can but doesn't need to be implemented by
 * an MQTT broker.
 */
public class Broker implements CommunicationEndpoint {

    private static record Subscription(List<String> topicFilter, Subscriber subscriber) {

        public Subscription(String topicFilter, Subscriber subscriber) {
            this(getTokensWithCollection(topicFilter), subscriber);
        }

        public boolean matches(String msgTopic) {
            return new Matcher(getTokensWithCollection(msgTopic), topicFilter).check();
        }

        private static List<String> getTokensWithCollection(String str) {
            return Collections.list(new StringTokenizer(str, "/")).stream().map(token -> (String) token).collect(Collectors.toList());
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

    private final List<Subscription> subscriptions = new LinkedList<>();
    private final String identifier;

    public Broker(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void subscribe(String topic, Subscriber subscriber) {
        subscribeRaw(identifier + topic, subscriber);
    }

    @Override
    public void subscribeRaw(String topic, Subscriber subscriber) {
        var s = new Subscription(topic, subscriber);
        subscriptions.add(s);
        System.out.println("Subscribed to: \t" + topic);
    }

    @Override
    public void unsubscribe(String topic, Subscriber subscriber) {
        unsubscribeRaw(identifier + topic, subscriber);
    }

    @Override
    public void unsubscribeRaw(String topic, Subscriber subscriber) {
        var s = new Subscription(topic, subscriber);
        subscriptions.remove(s);
        System.out.println("Unsubscribed from:\t" + topic);
    }

    @Override
    public void publish(Posting posting) {
        Posting toPublish = rewriteTopicToIncludeMe(posting);
        executePublish(toPublish);
        System.out.println("Published:\t" + toPublish.topic());
    }

    @Override
    public String ID() {
        return identifier;
    }

    private Posting rewriteTopicToIncludeMe(Posting posting) {
        return posting.cloneWithTopicAffix(identifier);
    }

    private void executePublish(Posting toPublish) {
        var subs = new LinkedList<>(subscriptions);
        for (Subscription subscription : subs) {
            deliverIfTopicMatches(toPublish, subscription);
        }
    }

    private void deliverIfTopicMatches(Posting msg, Subscription subscription) {
        if (subscription.matches(msg.topic())) subscription.subscriber().deliver(msg);
    }

}
