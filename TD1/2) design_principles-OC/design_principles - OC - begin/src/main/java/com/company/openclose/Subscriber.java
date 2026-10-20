package com.company.openclose;

import java.time.LocalDateTime;
import java.util.Objects;

/** Common identity, contact details and usage source for every subscriber. */
public abstract class Subscriber {

    private Long subscriberId;
    private String address;
    private Long phoneNumber;
    private int baseRate;
    private final UsageHistory usageHistory;

    protected Subscriber(UsageHistory usageHistory) {
        this.usageHistory = Objects.requireNonNull(usageHistory, "usageHistory must not be null");
    }

    /** Each subscriber type applies its own billing rules. */
    public abstract double calculateBill();

    /** Records usage without making the caller depend on a concrete history class. */
    public void addUsage(LocalDateTime begin, long quantity) {
        usageHistory.addSession(subscriberId, begin, quantity);
    }

    protected long getTotalUsage() {
        return usageHistory.getTotalUsage(subscriberId);
    }

    public Long getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(Long subscriberId) {
        this.subscriberId = subscriberId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(int baseRate) {
        this.baseRate = baseRate;
    }
}
