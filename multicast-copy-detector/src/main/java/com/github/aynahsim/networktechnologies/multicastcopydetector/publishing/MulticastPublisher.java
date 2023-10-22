package com.github.aynahsim.networktechnologies.multicastcopydetector.publishing;

import com.github.aynahsim.networktechnologies.multicastcopydetector.exceptions.MulticastPublisherException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MulticastPublisher implements Runnable {
    private static final String MULTICAST_MESSAGE = "I am alive";

    private static final long INITIAL_DELAY = 0;
    private static final long PERIOD = 1;

    private static final Logger logger = LoggerFactory.getLogger(MulticastPublisher.class);

    private final InetAddress multicastGroupAddress;
    private final int multicastGroupPort;

    private final Object multicastPublisherThreadLock = new Object();

    private boolean isAlive = true;

    public MulticastPublisher(String multicastGroupAddress, int multicastGroupPort) throws MulticastPublisherException {
        try {
            this.multicastGroupAddress = InetAddress.getByName(multicastGroupAddress);
        } catch (UnknownHostException | SecurityException exception) {
            throw new MulticastPublisherException(
                    "Failed to initialize multicast publisher. " + exception.getMessage(),
                    exception
            );
        }

        this.multicastGroupPort = multicastGroupPort;
    }

    @Override
    public void run() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                try {
                    if (isAlive) {
                        datagramSocket.send(new DatagramPacket(
                                MULTICAST_MESSAGE.getBytes(),
                                MULTICAST_MESSAGE.getBytes().length,
                                multicastGroupAddress,
                                multicastGroupPort
                        ));
                    } else {
                        scheduledExecutorService.shutdown();

                        synchronized (multicastPublisherThreadLock) {
                            multicastPublisherThreadLock.notify();
                        }
                    }
                } catch (IOException exception) {
                    logger.warn("Failed to send multicast message. " + exception.getMessage(), exception);
                }
            }, INITIAL_DELAY, PERIOD, TimeUnit.SECONDS);

            synchronized (multicastPublisherThreadLock) {
                try {
                    multicastPublisherThreadLock.wait();
                } catch (InterruptedException exception) {
                    logger.error(
                            "Thread " + Thread.currentThread() + " was interrupted. " + exception.getMessage(),
                            exception
                    );

                    scheduledExecutorService.shutdown();
                }
            }
        } catch (IOException exception) {
            logger.error("Failed to initialize datagram socket. " + exception.getMessage(), exception);
        }
    }

    public void kill() {
        isAlive = false;
    }
}