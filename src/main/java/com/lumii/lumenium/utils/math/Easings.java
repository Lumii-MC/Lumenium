package com.lumii.lumenium.utils.math;

import java.util.HashMap;

// Simple easing class. Derived from LodestoneLib's easing, which contains tweening functions by Robert Penner

public abstract class Easings {

    public static final HashMap<String, Easings> EASING = new HashMap<>();
    public final String type;
    public Easings(String type){
        this.type = type;
        EASING.put(this.type, this);
    }

    public static Easings valueof(){
        return EASING.get("type");
    }
    /**
     * easing function
     * @param t time
     * @param b beginning value
     * @param c changed value
     * @param d duration
     * @return eased value
     */
    public abstract float ease(float t, float b, float c, float d);

    //linear
    public static final Easings LINEAR = new Easings("linear") {
        @Override
        public float ease(float t, float b, float c, float d) {
            return c*t/d+b;
        }
    };

    // quadratics //
    public static final Easings QUAD_IN = new Easings("quadIn") {
        @Override
        public float ease(float t, float b, float c, float d) {
            return -c*(t/=d)*(t-2)+b;
        }
    };

    public static final Easings QUAD_OUT = new Easings("quadOut") {
        public float ease(float t, float b, float c, float d) {
            return -c * (t /= d) * (t - 2) + b;
        }
    };

    public static final Easings QUAD_IN_OUT = new Easings("quadInOut") {
        public float ease(float t, float b, float c, float d) {
            if ((t /= d / 2) < 1) return c / 2 * t * t + b;
            return -c / 2 * ((--t) * (t - 2) - 1) + b;
        }
    };

    // cubics //

    public static final Easings CUBIC_IN = new Easings("cubicIn") {
        public float ease(float t, float b, float c, float d) {
            return c * (t /= d) * t * t + b;
        }
    };

    public static final Easings CUBIC_OUT = new Easings("cubicOut") {
        public float ease(float t, float b, float c, float d) {
            return c * ((t = t / d - 1) * t * t + 1) + b;
        }
    };

    public static final Easings CUBIC_IN_OUT = new Easings("cubicInOut") {
        public float ease(float t, float b, float c, float d) {
            if ((t /= d / 2) < 1) return c / 2 * t * t * t + b;
            return c / 2 * ((t -= 2) * t * t + 2) + b;
        }
    };

    // quartics //

    public static final Easings QUART_IN = new Easings("quartIn") {
        public float ease(float t, float b, float c, float d) {
            return c * (t /= d) * t * t * t + b;
        }
    };

    public static final Easings QUART_OUT = new Easings("quartOut") {
        public float ease(float t, float b, float c, float d) {
            return -c * ((t = t / d - 1) * t * t * t - 1) + b;
        }
    };

    public static final Easings QUART_IN_OUT = new Easings("quartInOut") {
        public float ease(float t, float b, float c, float d) {
            if ((t /= d / 2) < 1) return c / 2 * t * t * t * t + b;
            return -c / 2 * ((t -= 2) * t * t * t - 2) + b;
        }
    };

    // quintics //

    public static final Easings QUINT_IN = new Easings("quinticIn") {
        public float ease(float t, float b, float c, float d) {
            return c * (t /= d) * t * t * t * t + b;
        }
    };

    public static final Easings QUINT_OUT = new Easings("quinticOut") {
        public float ease(float t, float b, float c, float d) {
            return c * ((t = t / d - 1) * t * t * t * t + 1) + b;
        }
    };

    public static final Easings QUINT_IN_OUT = new Easings("quintInOut") {
        public float ease(float t, float b, float c, float d) {
            if ((t /= d / 2) < 1) return c / 2 * t * t * t * t * t + b;
            return c / 2 * ((t -= 2) * t * t * t * t + 2) + b;
        }
    };

    // exponential //

    public static final Easings EXPO_IN = new Easings("expoIn") {
        public float ease(float t, float b, float c, float d) {
            return (t == 0) ? b : c * (float) Math.pow(2, 10 * (t / d - 1)) + b;
        }
    };

    public static final Easings EXPO_OUT = new Easings("expoOut") {
        public float ease(float t, float b, float c, float d) {
            return (t == d) ? b + c : c * (-(float) Math.pow(2, -10 * t / d) + 1) + b;
        }
    };

    public static final Easings EXPO_IN_OUT = new Easings("expoInOut") {
        public float ease(float t, float b, float c, float d) {
            if (t == 0) return b;
            if (t == d) return b + c;
            if ((t /= d / 2) < 1) return c / 2 * (float) Math.pow(2, 10 * (t - 1)) + b;
            return c / 2 * (-(float) Math.pow(2, -10 * --t) + 2) + b;
        }
    };
}
