package ru.practicum.ewm.aggregator.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * In-memory matrix storing per-user weights, per-pair min-weight sums,
 * and per-event weight sums needed for the similarity formula:
 *
 * similarity(A,B) = S_min(A,B) / (sqrt(S_A) * sqrt(S_B))
 *
 * S_min(A,B) = sum over users u of min(w_u_A, w_u_B)
 * S_A        = sum over users u of w_u_A
 */
@Component
public class SimilarityMatrix {

    // userId -> eventId -> weight
    private final Map<Long, Map<Long, Double>> userEventWeights = new ConcurrentHashMap<>();

    // eventId -> sum of weights across all users
    private final Map<Long, Double> eventWeightSum = new ConcurrentHashMap<>();

    // ordered pair (minId, maxId) -> sum of min(w_u_A, w_u_B) across all users
    private final Map<Long, Map<Long, Double>> pairMinWeightSum = new ConcurrentHashMap<>();

    /**
     * Returns the current weight for (userId, eventId), or 0.0 if not present.
     */
    public double getWeight(long userId, long eventId) {
        Map<Long, Double> userMap = userEventWeights.get(userId);
        if (userMap == null) return 0.0;
        return userMap.getOrDefault(eventId, 0.0);
    }

    /**
     * Updates the weight for (userId, eventId) if newWeight > oldWeight.
     * Also updates the weight sum for eventId.
     * Returns true if the weight was actually changed.
     */
    public boolean updateWeight(long userId, long eventId, double newWeight) {
        double currentWeight = getWeight(userId, eventId);
        if (newWeight <= currentWeight) return false;

        userEventWeights
                .computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .put(eventId, newWeight);

        // update weight sum: remove old contribution, add new
        eventWeightSum.merge(eventId, newWeight - currentWeight, Double::sum);
        return true;
    }

    /**
     * Returns all known event IDs across all users.
     */
    public Set<Long> getAllEvents() {
        Set<Long> events = Collections.newSetFromMap(new ConcurrentHashMap<>());
        for (Map<Long, Double> userMap : userEventWeights.values()) {
            events.addAll(userMap.keySet());
        }
        return events;
    }

    /**
     * Updates the min-weight sum for the pair (eventId, otherEventId).
     * Called after weight of eventId changed from oldWeight to newWeight for a specific user
     * who also has otherWeight for otherEventId.
     */
    public void updateMinWeightSum(
            long eventId,
            long otherEventId,
            double oldWeight,
            double newWeight,
            double otherWeight) {
        long first = Math.min(eventId, otherEventId);
        long second = Math.max(eventId, otherEventId);

        double oldMin = Math.min(oldWeight, otherWeight);
        double newMin = Math.min(newWeight, otherWeight);
        double delta = newMin - oldMin;

        pairMinWeightSum
                .computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .merge(second, delta, Double::sum);
    }

    /**
     * Computes similarity between eventId and otherEventId.
     * similarity = S_min(A,B) / (sqrt(S_A) * sqrt(S_B))
     * Returns 0.0 if either weight sum is zero.
     */
    public double computeSimilarity(long eventId, long otherEventId) {
        long first = Math.min(eventId, otherEventId);
        long second = Math.max(eventId, otherEventId);

        Map<Long, Double> innerMap = pairMinWeightSum.get(first);
        double minSum = (innerMap == null) ? 0.0 : innerMap.getOrDefault(second, 0.0);
        if (minSum <= 0.0) return 0.0;

        double sumA = eventWeightSum.getOrDefault(eventId, 0.0);
        double sumB = eventWeightSum.getOrDefault(otherEventId, 0.0);
        if (sumA <= 0.0 || sumB <= 0.0) return 0.0;

        return minSum / (Math.sqrt(sumA) * Math.sqrt(sumB));
    }
}
