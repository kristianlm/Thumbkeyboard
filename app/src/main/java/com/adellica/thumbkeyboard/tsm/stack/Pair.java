package com.adellica.thumbkeyboard.tsm.stack;

import com.adellica.thumbkeyboard.tsm.Machine;

public class Pair extends APair implements Machine.Applicable {
    private final Object __car;
    private final IPair __cdr;

    public Pair(Object car, IPair cdr) {
        __car = car;
        __cdr = cdr;
    }

    public Object car() {
        return __car;
    }

    public IPair cdr() {
        return __cdr;
    }

    public static final Pair nil = new Pair(null, null) {
        public <T> T car(Class<T> t) {
            throw new Machine.EmptyStackPopped();
        }
        public Object car() {
            throw new Machine.EmptyStackPopped();
        }
        public Pair cdr() {
            throw new Machine.EmptyStackPopped();
        }
    };

    public static Pair cons(Object car, IPair cdr) {
        return new Pair(car, cdr);
    }

    public static Pair list(Object... args) {
        Pair p = nil;
        for (int i = args.length - 1; i >= 0; i--) {
            p = cons(args[i], p);
        }
        return p;
    }

    public static Pair reverse(IPair pair) {
        Pair result = nil;
        while (pair != nil) {
            result = cons(pair.car(), result);
            pair = pair.cdr();
        }
        return result;
    }

}
