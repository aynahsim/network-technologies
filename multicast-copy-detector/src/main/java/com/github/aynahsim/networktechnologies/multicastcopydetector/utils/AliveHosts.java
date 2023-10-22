package com.github.aynahsim.networktechnologies.multicastcopydetector.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class AliveHosts {
    private static final long HOST_TIMEOUT = 2000;

    private static final Logger logger = LoggerFactory.getLogger(AliveHosts.class);

    private final ConcurrentHashMap<InetSocketAddress, Long> aliveHosts = new ConcurrentHashMap<>();

    public void updateHostState(InetSocketAddress host) {
        long lastActivity = System.currentTimeMillis();

        if (aliveHosts.containsKey(host)) {
            aliveHosts.put(host, lastActivity);
        } else {
            logger.info("New host: {}", host);
            aliveHosts.put(host, lastActivity);
            logAliveHosts();
        }
    }

    public void cleanUp() {
        for (final InetSocketAddress host : aliveHosts.keySet()) {
            long lastActivity = aliveHosts.get(host);
            long currentTime = System.currentTimeMillis();
            long inactiveTime = currentTime - lastActivity;

            if (inactiveTime >= HOST_TIMEOUT) {
                aliveHosts.remove(host);
                logger.info("Host removed: {}", host);

                logAliveHosts();
            } else {
                updateHostState(host);
            }
        }
    }

    private void logAliveHosts() {
        logger.info("--------------------------------------------------");
        logger.info("Alive hosts ({}):", aliveHosts.size());

        String lastActivityFormatPattern = "dd.MM.yyyy HH:mm:ss:SSS";
        SimpleDateFormat lastActivityFormat = new SimpleDateFormat(lastActivityFormatPattern);
        for (final InetSocketAddress host : aliveHosts.keySet()) {
            logger.info(
                    "\t{} (last activity: {})",
                    host,
                    lastActivityFormat.format(new Date(aliveHosts.get(host)))
            );
        }

        logger.info("--------------------------------------------------");
    }
}