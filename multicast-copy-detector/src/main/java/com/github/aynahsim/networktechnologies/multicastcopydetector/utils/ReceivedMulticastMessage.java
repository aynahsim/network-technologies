package com.github.aynahsim.networktechnologies.multicastcopydetector.utils;

import java.net.InetSocketAddress;

public record ReceivedMulticastMessage(InetSocketAddress host, String messageText) {
}