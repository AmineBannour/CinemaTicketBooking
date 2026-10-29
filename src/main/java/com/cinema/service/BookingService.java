package com.cinema.service;

import com.cinema.exception.BookingException;
import com.cinema.model.Booking;
import com.cinema.model.Screening;
import com.cinema.model.StandardTicket;
import com.cinema.model.StudentTicket;
import com.cinema.model.Ticket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BookingService {
    private final List<Screening> screenings;
    private final List<Booking> bookings = new ArrayList<>();
    private int nextBookingId = 1;

    public BookingService(List<Screening> screenings) {
        if (screenings == null || screenings.isEmpty()) {
            throw new IllegalArgumentException("At least one screening is required.");
        }
        Set<Integer> ids = new HashSet<>();
        for (Screening screening : screenings) {
            if (screening == null || !ids.add(screening.getId())) {
                throw new IllegalArgumentException("Screenings must have unique IDs and cannot be null.");
            }
        }
        this.screenings = List.copyOf(screenings);
    }

    public Optional<Screening> findScreening(int id) {
        for (Screening screening : screenings) {
            if (screening.getId() == id) {
                return Optional.of(screening);
            }
        }
        return Optional.empty();
    }

    public Booking bookTickets(int screeningId, String customerName, boolean student, int quantity)
            throws BookingException {
        Optional<Screening> result = findScreening(screeningId);
        if (result.isEmpty()) {
            throw new BookingException("Screening not found.");
        }
        Screening screening = result.get();
        if (customerName == null || customerName.isBlank()) {
            throw new BookingException("Please enter a customer name.");
        }
        if (quantity <= 0) {
            throw new BookingException("The number of tickets must be positive.");
        }
        if (quantity > screening.getAvailableSeats()) {
            throw new BookingException("Only " + screening.getAvailableSeats() + " seats are available.");
        }

        Ticket ticket;
        if (student) {
            ticket = new StudentTicket(screening);
        } else {
            ticket = new StandardTicket(screening);
        }
        Booking booking = new Booking(nextBookingId, customerName, ticket, quantity);
        // The Booking constructor reserves seats after validating its inputs.
        bookings.add(booking);
        nextBookingId++;
        return booking;
    }

    public void cancelBooking(int bookingId) throws BookingException {
        for (Booking booking : bookings) {
            if (booking.getId() == bookingId) {
                if (booking.isCancelled()) {
                    throw new BookingException("This booking is already cancelled.");
                }
                booking.cancel();
                return;
            }
        }
        throw new BookingException("Booking not found.");
    }

    public List<Screening> getScreenings() {
        return screenings;
    }

    public List<Booking> getBookings() {
        return List.copyOf(bookings);
    }

    public long getRevenueCents() {
        long total = 0;
        for (Booking booking : bookings) {
            if (!booking.isCancelled()) {
                total += booking.getTotalPriceCents();
            }
        }
        return total;
    }
}
