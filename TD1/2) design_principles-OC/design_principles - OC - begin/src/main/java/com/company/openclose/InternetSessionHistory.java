package com.company.openclose;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Stores internet sessions, with their usage expressed in data units. */
public class InternetSessionHistory implements UsageHistory {

    public static class InternetSession {
        private final LocalDateTime begin;
        private final Long subscriberId;
        private final long dataUsed;

        public InternetSession(Long subscriberId, LocalDateTime begin, long dataUsed) {
            this.begin = begin;
            this.dataUsed = dataUsed;
            this.subscriberId = subscriberId;
        }

        public LocalDateTime getBegin() {
            return begin;
        }

        public long getDataUsed() {
            return dataUsed;
        }

        public Long getSubscriberId() {
            return subscriberId;
        }
    }

    private final Map<Long, List<InternetSession>> sessionsBySubscriber = new HashMap<>();

    public synchronized List<InternetSession> getCurrentSessions(Long subscriberId) {
        List<InternetSession> sessions = sessionsBySubscriber.get(subscriberId);
        return sessions == null
                ? Collections.<InternetSession>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(sessions));
    }

    @Override
    public synchronized void addSession(Long subscriberId, LocalDateTime begin, long dataUsed) {
        sessionsBySubscriber.computeIfAbsent(subscriberId, id -> new ArrayList<>())
                .add(new InternetSession(subscriberId, begin, dataUsed));
    }

    @Override
    public synchronized long getTotalUsage(Long subscriberId) {
        List<InternetSession> sessions = sessionsBySubscriber.get(subscriberId);
        if (sessions == null) {
            return 0;
        }
        return sessions.stream().mapToLong(InternetSession::getDataUsed).sum();
    }
}
