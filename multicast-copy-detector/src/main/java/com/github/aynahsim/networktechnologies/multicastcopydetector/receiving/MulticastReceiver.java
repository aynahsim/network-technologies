package com.github.aynahsim.networktechnologies.multicastcopydetector.receiving;

import com.github.aynahsim.networktechnologies.multicastcopydetector.exceptions.ConfiguringSocketException;
import com.github.aynahsim.networktechnologies.multicastcopydetector.exceptions.MulticastReceiverException;
import com.github.aynahsim.networktechnologies.multicastcopydetector.utils.MulticastMessageHandler;
import com.github.aynahsim.networktechnologies.multicastcopydetector.utils.ReceivedMulticastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class MulticastReceiver implements Runnable {
    private static final int RECEIVING_BUFFER_SIZE = 1024;
    private static final int RECEIVING_TIMEOUT = 2000;
    private static final int DATAGRAM_PACKET_OFFSET = 0;

    private static final Logger logger = LoggerFactory.getLogger(MulticastReceiver.class);

    private final InetSocketAddress multicastGroup;
    private final int multicastGroupPort;
    private final NetworkInterface networkInterface;
    private final MulticastMessageHandler multicastMessageHandler;

    private boolean isAlive = true;

    public MulticastReceiver(
            String multicastGroupAddress,
            int multicastGroupPort,
            String networkInterfaceName,
            MulticastMessageHandler multicastMessageHandler
    ) throws MulticastReceiverException {
        try {
            multicastGroup = new InetSocketAddress(InetAddress.getByName(multicastGroupAddress), multicastGroupPort);
            this.multicastGroupPort = multicastGroupPort;
            networkInterface = NetworkInterface.getByName(networkInterfaceName);
            this.multicastMessageHandler = multicastMessageHandler;
        } catch (UnknownHostException | SecurityException | SocketException | NullPointerException exception) {
            throw new MulticastReceiverException("Failed to initialize multicast receiver. " + exception.getMessage(), exception);
        }
    }

    @Override
    public void run() {
        try (MulticastSocket multicastSocket = new MulticastSocket(multicastGroupPort)) {
            configureSocket(multicastSocket);

            try {
                while (isAlive) {
                    byte[] receivingBuffer = new byte[RECEIVING_BUFFER_SIZE];
                    DatagramPacket receivingPacket = new DatagramPacket(receivingBuffer, RECEIVING_BUFFER_SIZE);

                    multicastSocket.receive(receivingPacket);
                    multicastMessageHandler.handleMessage(new ReceivedMulticastMessage(
                            new InetSocketAddress(receivingPacket.getAddress(), receivingPacket.getPort()),
                            new String(
                                    receivingPacket.getData(),
                                    DATAGRAM_PACKET_OFFSET,
                                    receivingPacket.getLength()
                            )
                    ));
                }
            } catch (SocketTimeoutException exception) {
                logger.warn("Multicast socket timed out. " + exception.getMessage(), exception);
            } catch (IOException exception) {
                logger.warn("Failed to receive multicast message. " + exception.getMessage(), exception);
            } finally {
                multicastSocket.leaveGroup(multicastGroup, networkInterface);
            }
        } catch (IOException | SecurityException exception) {
            logger.error("Failed to initialize multicast socket. " + exception.getMessage(), exception);
        } catch (ConfiguringSocketException exception) {
            logger.error("Failed to configure multicast socket. " + exception.getMessage(), exception);
        }
    }

    public void kill() {
        isAlive = false;
    }

    private void configureSocket(MulticastSocket multicastSocket) throws ConfiguringSocketException {
        try {
            multicastSocket.joinGroup(multicastGroup, networkInterface);
            multicastSocket.setSoTimeout(RECEIVING_TIMEOUT);
        } catch (IOException | SecurityException | IllegalArgumentException exception) {
            throw new ConfiguringSocketException(
                    "Failed to configure multicast socket. " + exception.getMessage(),
                    exception
            );
        }
    }
}