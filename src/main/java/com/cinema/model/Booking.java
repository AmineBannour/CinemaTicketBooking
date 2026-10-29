package com.cinema.model;

public class Booking {
    private final int id;
    private final String customerName;
    private final Ticket ticket;
    private final int quantity;
    private final long totalPriceCents;
    private boolean cancelled;

    public Booking(int id, String customerName, Ticket ticket, int quantity) {
        if (id <= 0 || customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("A positive ID and customer name are required.");
        }
        if (ticket == null || quantity <= 0) {
            throw new IllegalArgumentException("A ticket and positive quantity are required.");
        }
        this.id = id;
        this.customerName = customerName.trim();
        this.ticket = ticket;
        this.quantity = quantity;
        this.totalPriceCents = (long) ticket.getPriceCents() * quantity;
        // A booking reserves its own seats and returns them when cancelled.
        ticket.getScreening().reserveSeats(quantity);
    }

    public void cancel() {
        if (cancelled) {
            throw new IllegalStateException("This booking is already cancelled.");
        }
        ticket.getScreening().releaseSeats(quantity);
        cancelled = true;
    }

    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getTotalPriceCents() {
        return totalPriceCents;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
