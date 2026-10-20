package com.company.openclose;

/**
 * Contract for retrieving and recording a subscriber's usage.
 * The quantity is expressed in the unit appropriate to the implementation
 * (for example, minutes for calls or data units for internet sessions).
 */
import java.time.LocalDateTime;

public interface UsageHistory {

    long getTotalUsage(Long subscriberId);

    void addSession(Long subscriberId, LocalDateTime begin, long quantity);
}
