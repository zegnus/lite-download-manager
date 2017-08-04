package com.zegnus.litedownloadmanager;

import java.util.concurrent.TimeUnit;

class CallbackThrottleCreator {

    enum Type {
        THROTTLE_BY_TIME,
        THROTTLE_BY_PROGRESS_INCREASE;
    }

    private final Type type;

    private final TimeUnit timeUnit;
    private final long frequency;

    static CallbackThrottleCreator ByTime(TimeUnit timeUnit, long quantity) {
        return new CallbackThrottleCreator(Type.THROTTLE_BY_TIME, timeUnit, quantity);
    }

    static CallbackThrottleCreator ByProgressIncrease() {
        return new CallbackThrottleCreator(Type.THROTTLE_BY_PROGRESS_INCREASE, TimeUnit.SECONDS, 0);
    }

    private CallbackThrottleCreator(Type type, TimeUnit timeUnit, long frequency) {
        this.type = type;
        this.timeUnit = timeUnit;
        this.frequency = frequency;
    }

    CallbackThrottle create() {
        switch (type) {
            case THROTTLE_BY_TIME:
                return new CallbackThrottleByTime(timeUnit.toMillis(frequency));
            case THROTTLE_BY_PROGRESS_INCREASE:
                return new CallbackThrottleByProgressIncrease();
            default:
                throw new IllegalStateException("type " + type + " not supported");
        }
    }
}
