package com.adellica.thumbkeyboard.tsm.stack;

import static com.adellica.thumbkeyboard.tsm.stack.Pair.cons;
import static com.adellica.thumbkeyboard.tsm.stack.Pair.nil;

public class Stack {
    IPair at;
    public Stack(IPair at) {
        if(at == null) throw new RuntimeException("Pair cannot be null (use Pair.nil for '())");
        this.at = at;
    }

    public void push(Object o) {
        at = cons(o, at);
    }
    public Object pop() {
        Object t = at.car();
        at = at.cdr();
        return t;
    }
    public <T> T pop(Class<T> c) {
        T t = at.car(c);
        at = at.cdr();
        return t;
    }
    public IPair peek() {
        return at;
    }
    public boolean empty() {
        return at == nil;
    }
}
