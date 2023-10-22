package com.github.aynahsim.networktechnologies.multicastcopydetector.exceptions;

public class MulticastCopyDetectorException extends RuntimeException {
    public MulticastCopyDetectorException(String message) {
        super(message);
    }

    public MulticastCopyDetectorException(String message, Throwable cause) {
        super(message, cause);
    }
}