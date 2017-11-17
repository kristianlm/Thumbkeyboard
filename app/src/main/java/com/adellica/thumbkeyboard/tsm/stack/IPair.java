package com.adellica.thumbkeyboard.tsm.stack;

public interface IPair {
    Object car();
    IPair cdr();
    <T> T car(Class<T> t);
}
