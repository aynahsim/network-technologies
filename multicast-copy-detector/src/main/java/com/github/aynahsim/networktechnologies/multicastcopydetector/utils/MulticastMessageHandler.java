package com.github.aynahsim.networktechnologies.multicastcopydetector.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MulticastMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MulticastMessageHandler.class);

    private final AliveHosts aliveHosts;

    public MulticastMessageHandler(AliveHosts aliveHosts) {
        this.aliveHosts = aliveHosts;
    }

    public void handleMessage(ReceivedMulticastMessage receivedMulticastMessage) {
        aliveHosts.updateHostState(receivedMulticastMessage.host());

        logger.debug(
                "New message from {}: {}",
                receivedMulticastMessage.host(),
                receivedMulticastMessage.messageText()
        );
    }
}