package com.company.openclose;

/** Subscriber billed for internet data above the included allowance. */
public class ISPSubscriber extends Subscriber {

    private long freeUsage;

    public ISPSubscriber() {
        this(new InternetSessionHistory());
    }

    public ISPSubscriber(UsageHistory internetHistory) {
        super(internetHistory);
    }

    @Override
    public double calculateBill() {
        long chargeableData = getTotalUsage() - freeUsage;
        return chargeableData * getBaseRate() / 100;
    }

    public long getFreeUsage() {
        return freeUsage;
    }

    public void setFreeUsage(long freeUsage) {
        this.freeUsage = freeUsage;
    }
}
