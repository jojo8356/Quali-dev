package com.company.openclose;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import org.junit.Test;

public class UsageHistoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 20, 12, 0);

    @Test
    public void phoneSubscriberUsesCallHistoryThroughTheInterface() {
        UsageHistory history = new CallHistory();
        PhoneSubscriber subscriber = new PhoneSubscriber(history);
        subscriber.setSubscriberId(42L);
        subscriber.setBaseRate(10);

        subscriber.addUsage(NOW, 30);
        subscriber.addUsage(NOW.plusMinutes(1), 20);

        assertEquals(5.0, subscriber.calculateBill(), 0.0);
        assertEquals(50L, history.getTotalUsage(42L));
    }

    @Test
    public void internetSubscriberBillsOnlyUsageAboveAllowance() {
        UsageHistory history = new InternetSessionHistory();
        ISPSubscriber subscriber = new ISPSubscriber(history);
        subscriber.setSubscriberId(7L);
        subscriber.setBaseRate(2);
        subscriber.setFreeUsage(100L);

        subscriber.addUsage(NOW, 60);
        subscriber.addUsage(NOW.plusMinutes(1), 90);

        assertEquals(1.0, subscriber.calculateBill(), 0.0);
        assertEquals(150L, history.getTotalUsage(7L));
    }

    @Test
    public void historiesKeepUsageSeparateBySubscriberId() {
        UsageHistory history = new CallHistory();
        history.addSession(1L, NOW, 10);
        history.addSession(2L, NOW, 25);

        assertEquals(10L, history.getTotalUsage(1L));
        assertEquals(25L, history.getTotalUsage(2L));
        assertEquals(0L, history.getTotalUsage(3L));
    }
}
