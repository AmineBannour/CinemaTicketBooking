package com.cinema.model;

/** A ticket has a screening, but its price depends on the ticket type. */
public abstract class Ticket {
    private final Screening screening;

    public Ticket(Screening screening) {
        if (screening == null) {
            throw new IllegalArgumentException("A screening is required.");
        }
        this.screening = screening;
    }

    public Screening getScreening() {
        return screening;
    }

    public abstract int getPriceCents();

    public abstract String getType();
}
