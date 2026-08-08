package com.microsoft.playwright.extension.exception;

/**
 * Runtime exception thrown when an error occurs during Playwright browser automation operations.
 * Supports multiple constructor patterns for flexible error reporting with error codes,
 * descriptions, and root causes.
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 */
public class PlaywrightException extends RuntimeException {

    private static final long serialVersionUID = -7545341502620139031L;

    /**
     * Constructs a new Playwright exception with the specified error code.
     *
     * @param errorCode the error code identifying the type of error
     */
    public PlaywrightException(String errorCode){
        super(errorCode);
    }

    /**
     * Constructs a new Playwright exception with the specified error code and root cause.
     *
     * @param errorCode the error code identifying the type of error
     * @param cause     the underlying cause of this exception
     */
    public PlaywrightException(String errorCode, Throwable cause){
        super(errorCode, cause);
    }

    /**
     * Constructs a new Playwright exception with the specified error code and description.
     * The resulting message is formatted as {@code "errorCode:errorDesc"}.
     *
     * @param errorCode  the error code identifying the type of error
     * @param errorDesc  a human-readable description of the error
     */
    public PlaywrightException(String errorCode, String errorDesc){
        super(errorCode + ":" + errorDesc);
    }

    /**
     * Constructs a new Playwright exception with the specified error code, description, and root cause.
     * The resulting message is formatted as {@code "errorCode:errorDesc"}.
     *
     * @param errorCode  the error code identifying the type of error
     * @param errorDesc  a human-readable description of the error
     * @param cause      the underlying cause of this exception
     */
    public PlaywrightException(String errorCode, String errorDesc, Throwable cause){
        super(errorCode + ":" + errorDesc, cause);
    }

    /**
     * Constructs a new Playwright exception wrapping the specified root cause.
     *
     * @param cause the underlying cause of this exception
     */
    public PlaywrightException(Throwable cause){
        super(cause);
    }
}
