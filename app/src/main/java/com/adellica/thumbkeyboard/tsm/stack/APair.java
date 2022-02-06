package com.adellica.thumbkeyboard.tsm.stack;

import com.adellica.thumbkeyboard.tsm.Machine;

import static com.adellica.thumbkeyboard.tsm.stack.Pair.nil;

abstract public class APair implements IPair, Machine.Applicable {
    @SuppressWarnings("unchecked")
    public <T> T car(Class<T> t) {
        if (t.isInstance(car())) return (T) car();
        throw new Machine.TypeMismatch(t, car());
    }

    public String toStringParenless() {
        if (this == nil) return "";
        StringBuilder f = new StringBuilder("" + car());
        IPair p = cdr();
        while (p != nil) {
            f.append(" ").append(p.car());
            p = p.cdr();
        }
        return f.toString();
    }

    public String toString() {
        if (this == nil) return "[ ]";
        return "[ " + toStringParenless() + " ]";
    }

    @Override
    public void exe(Machine m) {
        IPair ip = this;
        while (ip != nil) {
            m.eval(ip.car());
            ip = ip.cdr();
        }
    }
}
