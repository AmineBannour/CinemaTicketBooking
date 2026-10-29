package com.cinema.exception;

/** A booking request that cannot be completed, such as a sold-out screening. */
public class BookingException extends Exception {
    public BookingException(String message) {
        super(message);
    }
}
