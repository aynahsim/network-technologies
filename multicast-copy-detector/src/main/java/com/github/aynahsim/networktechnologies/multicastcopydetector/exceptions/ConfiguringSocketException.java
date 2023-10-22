package com.github.aynahsim.networktechnologies.multicastcopydetector.exceptions;

public class ConfiguringSocketException extends MulticastReceiverException {
    public ConfiguringSocketException(String message) {
        super(message);
    }

    public ConfiguringSocketException(String message, Throwable cause) {
        super(message, cause);
    }
}