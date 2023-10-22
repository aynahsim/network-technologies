package com.github.aynahsim.networktechnologies.multicastcopydetector;

import com.github.aynahsim.networktechnologies.multicastcopydetector.exceptions.MulticastPublisherException;
import com.github.aynahsim.networktechnologies.multicastcopydetector.exceptions.MulticastReceiverException;
import com.github.aynahsim.networktechnologies.multicastcopydetector.publishing.MulticastPublisher;
import com.github.aynahsim.networktechnologies.multicastcopydetector.receiving.MulticastReceiver;
import com.github.aynahsim.networktechnologies.multicastcopydetector.utils.AliveHosts;
import com.github.aynahsim.networktechnologies.multicastcopydetector.utils.DeadHostCleaner;
import com.github.aynahsim.networktechnologies.multicastcopydetector.utils.MulticastMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.Timer;

public class Main {
    private static final int MULTICAST_GROUP_ADDRESS_INDEX = 0;
    private static final int MULTICAST_GROUP_PORT_INDEX = 1;
    private static final int NETWORK_INTERFACE_NAME_INDEX = 2;

    private static final String DEFAULT_MULTICAST_GROUP_ADDRESS = "230.0.0.1";
    private static final int DEFAULT_MULTICAST_GROUP_PORT = 8080;
    private static final String DEFAULT_NETWORK_INTERFACE_NAME = "em0";

    private static final long DEAD_HOST_CLEANER_DELAY = 1000;
    private static final long DEAD_HOST_CLEANER_PERIOD = 2000;

    private static final String EXIT = "exit";

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static String multicastGroupAddress;
    private static int multicastGroupPort;
    private static String networkInterfaceName;

    public static void main(String[] args) {
        fillMulticastProperties(args);
        logMulticastProperties();

        AliveHosts aliveHosts = new AliveHosts();

        Timer deadHostCleanerTimer = new Timer();
        deadHostCleanerTimer.schedule(
                new DeadHostCleaner(aliveHosts),
                DEAD_HOST_CLEANER_DELAY,
                DEAD_HOST_CLEANER_PERIOD
        );

        MulticastPublisher multicastPublisher;
        try {
            multicastPublisher = new MulticastPublisher(multicastGroupAddress, multicastGroupPort);
        } catch (MulticastPublisherException exception) {
            logger.error(exception.getMessage(), exception);
            return;
        }

        Thread multicastPublisherThread = new Thread(multicastPublisher, "Multicast Publisher");
        multicastPublisherThread.start();

        MulticastReceiver multicastReceiver;
        try {
            multicastReceiver = new MulticastReceiver(
                    multicastGroupAddress,
                    multicastGroupPort,
                    networkInterfaceName,
                    new MulticastMessageHandler(aliveHosts)
            );
        } catch (MulticastReceiverException exception) {
            logger.error(exception.getMessage(), exception);
            return;
        }

        Thread multicastReceiverThread = new Thread(multicastReceiver, "Multicast Receiver");
        multicastReceiverThread.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (scanner.nextLine().equals(EXIT)) {
                break;
            }
        }

        multicastPublisher.kill();
        multicastReceiver.kill();
        deadHostCleanerTimer.cancel();

        try {
            multicastPublisherThread.join();
            multicastReceiverThread.join();
        } catch (InterruptedException exception) {
            logger.error("Failed to wait for multicast thread to die. " + exception.getMessage(), exception);
        }
    }

    private static void fillMulticastProperties(String[] source) {
        multicastGroupAddress = source.length > 0
                ? source[MULTICAST_GROUP_ADDRESS_INDEX]
                : DEFAULT_MULTICAST_GROUP_ADDRESS;

        try {
            multicastGroupPort = source.length > 1
                    ? Integer.parseInt(source[MULTICAST_GROUP_PORT_INDEX])
                    : DEFAULT_MULTICAST_GROUP_PORT;
        } catch (NumberFormatException exception) {
            logger.warn(
                    "Failed to parse multicast group port number from the second argument: {}. "
                            + "Using default multicast group port number: {}",
                    exception.getMessage(),
                    DEFAULT_MULTICAST_GROUP_PORT
            );
            multicastGroupPort = DEFAULT_MULTICAST_GROUP_PORT;
        }

        networkInterfaceName = source.length > 2
                ? source[NETWORK_INTERFACE_NAME_INDEX]
                : DEFAULT_NETWORK_INTERFACE_NAME;
    }

    private static void logMulticastProperties() {
        logger.debug("Multicast group address: {}", multicastGroupAddress);
        logger.debug("Multicast group port: {}", multicastGroupPort);
        logger.debug("Network interface name: {}", networkInterfaceName);
    }
}