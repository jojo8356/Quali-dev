package com.company.openclose;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Stores phone calls, with their duration expressed in minutes. */
public class CallHistory implements UsageHistory {

    public static class Call {
        private final LocalDateTime begin;
        private final long duration;
        private final Long subscriberId;

        public Call(Long subscriberId, LocalDateTime begin, long duration) {
            this.begin = begin;
            this.duration = duration;
            this.subscriberId = subscriberId;
        }

        public LocalDateTime getBegin() {
            return begin;
        }

        public long getDuration() {
            return duration;
        }

        public Long getSubscriberId() {
            return subscriberId;
        }
    }

    private final Map<Long, List<Call>> callsBySubscriber = new HashMap<>();

    public synchronized List<Call> getCurrentCalls(Long subscriberId) {
        List<Call> calls = callsBySubscriber.get(subscriberId);
        return calls == null
                ? Collections.<Call>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(calls));
    }

    @Override
    public synchronized void addSession(Long subscriberId, LocalDateTime begin, long duration) {
        callsBySubscriber.computeIfAbsent(subscriberId, id -> new ArrayList<>())
                .add(new Call(subscriberId, begin, duration));
    }

    @Override
    public synchronized long getTotalUsage(Long subscriberId) {
        List<Call> calls = callsBySubscriber.get(subscriberId);
        if (calls == null) {
            return 0;
        }
        return calls.stream().mapToLong(Call::getDuration).sum();
    }
}
