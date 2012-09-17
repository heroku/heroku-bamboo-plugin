package com.heroku.bamboo;

/**
 * @author Ryan Brainard
 */
public class HerokuBambooHandledException extends RuntimeException {
    public HerokuBambooHandledException() {
    }

    public HerokuBambooHandledException(String message) {
        super(message);
    }

    public HerokuBambooHandledException(String message, Throwable cause) {
        super(message, cause);
    }

    public HerokuBambooHandledException(Throwable cause) {
        super(cause);
    }
}
