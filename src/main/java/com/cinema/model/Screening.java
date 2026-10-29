package com.cinema.model;

public class Screening {
    private final int id;
    private final String movieTitle;
    private final int basePriceCents;
    private final int capacity;
    private int availableSeats;

    public Screening(int id, String movieTitle, int basePriceCents, int capacity) {
        if (id <= 0 || movieTitle == null || movieTitle.isBlank()) {
            throw new IllegalArgumentException("A positive ID and movie title are required.");
        }
        if (basePriceCents <= 0 || capacity <= 0) {
            throw new IllegalArgumentException("Ticket price and capacity must be positive.");
        }
        this.id = id;
        this.movieTitle = movieTitle.trim();
        this.basePriceCents = basePriceCents;
        this.capacity = capacity;
        this.availableSeats = capacity;
    }

    public void reserveSeats(int quantity) {
        if (quantity <= 0 || quantity > availableSeats) {
            throw new IllegalArgumentException("Invalid quantity or not enough seats.");
        }
        availableSeats -= quantity;
    }

    public void releaseSeats(int quantity) {
        if (quantity <= 0 || quantity > capacity - availableSeats) {
            throw new IllegalArgumentException("Cannot release more seats than were reserved.");
        }
        availableSeats += quantity;
    }

    public int getId() {
        return id;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public int getBasePriceCents() {
        return basePriceCents;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public int getCapacity() {
        return capacity;
    }
}
