package com.skydo.lib.fsm.definitions;

public @interface Transition {

    String fromState();

    String[] toState();
}
