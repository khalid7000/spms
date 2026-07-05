package com.rit.spms.exception;

/** Thrown when the Claude API call backing a SWOT AI feature fails or is unconfigured. */
public class AiUnavailableException extends RuntimeException {
    public AiUnavailableException(String message) {
        super(message);
    }

    public AiUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
