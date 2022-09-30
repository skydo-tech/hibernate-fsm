package com.skydo.lib.fsm.internal.tools;

public @interface Transition {

    String fromState();

    String[] toState();
}
