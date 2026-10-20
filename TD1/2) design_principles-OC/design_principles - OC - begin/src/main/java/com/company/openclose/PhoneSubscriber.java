package com.company.openclose;

/** Subscriber billed according to the total duration of phone calls. */
public class PhoneSubscriber extends Subscriber {

    public PhoneSubscriber() {
        this(new CallHistory());
    }

    public PhoneSubscriber(UsageHistory callHistory) {
        super(callHistory);
    }

    @Override
    public double calculateBill() {
        return getTotalUsage() * getBaseRate() / 100;
    }
}
