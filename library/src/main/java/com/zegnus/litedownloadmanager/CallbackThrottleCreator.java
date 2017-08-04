package com.zegnus.litedownloadmanager;

import java.util.concurrent.TimeUnit;

class CallbackThrottleCreator {

    enum Type {
        THROTTLE_BY_TIME,
        THROTTLE_BY_FREQUENCY
    }

    private final Type type;
    private final TimeUnit timeUnit;
    private final long frequency;

    static CallbackThrottleCreator ByTime(TimeUnit timeUnit, long quantity) {
        return new CallbackThrottleCreator(Type.THROTTLE_BY_TIME, timeUnit, quantity);
    }

    private CallbackThrottleCreator(Type type, TimeUnit timeUnit, long frequency) {
        this.type = type;
        this.timeUnit = timeUnit;
        this.frequency = frequency;
    }

    CallbackThrottle create() {
        if (type == null || timeUnit  == null) {
            throw new IllegalStateException("you must call setThrottleByTime first");
        }

        switch (type) {
            case THROTTLE_BY_TIME:
                return new LiteCallbackThrottleByTime(timeUnit.toMillis(frequency));
            case THROTTLE_BY_FREQUENCY:
                throw new IllegalStateException("type " + type + " not implemented yet");
            default:
                throw new IllegalStateException("type " + type + " not supported");
        }
    }
}
