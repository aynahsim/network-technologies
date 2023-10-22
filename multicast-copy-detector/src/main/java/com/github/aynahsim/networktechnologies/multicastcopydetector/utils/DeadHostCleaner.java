package com.github.aynahsim.networktechnologies.multicastcopydetector.utils;

import java.util.TimerTask;

public class DeadHostCleaner extends TimerTask {
    private final AliveHosts aliveHosts;

    public DeadHostCleaner(AliveHosts aliveHosts) {
        this.aliveHosts = aliveHosts;
    }

    @Override
    public void run() {
        aliveHosts.cleanUp();
    }
}