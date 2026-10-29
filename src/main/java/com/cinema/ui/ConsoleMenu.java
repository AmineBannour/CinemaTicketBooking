package com.cinema.ui;

import com.cinema.exception.BookingException;
import com.cinema.model.Booking;
import com.cinema.model.Screening;
import com.cinema.model.StandardTicket;
import com.cinema.model.StudentTicket;
import com.cinema.service.BookingService;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ConsoleMenu {
    private final BookingService service;
    private final Scanner input;
    private final PrintStream output;

    public ConsoleMenu(BookingService service, Scanner input, PrintStream output) {
        this.service = service;
        this.input = input;
        this.output = output;
    }

    public void run() {
        output.println("=== CINEMA TICKET BOOKING ===");
        output.println("Standard tickets: full price. Student tickets: 20% off.");
        output.println("No assigned seats. Cancellation gives a full refund.");
        output.println("Bookings are kept only until you exit the program.");
        try {
            boolean running = true;
            while (running) {
                output.println("\n1. Show screenings");
                output.println("2. Book tickets");
                output.println("3. Cancel a booking");
                output.println("4. Show bookings and revenue");
                output.println("5. Exit");
                int choice = readNumber("Your choice: ");
                switch (choice) {
                    case 1:
                        showScreenings();
                        break;
                    case 2:
                        bookTickets();
                        break;
                    case 3:
                        cancelBooking();
                        break;
                    case 4:
                        showBookings();
                        break;
                    case 5:
                        running = false;
                        break;
                    default:
                        output.println("Please choose a number from 1 to 5.");
                }
            }
            output.println("Goodbye!");
        } catch (NoSuchElementException e) {
            output.println("\nInput ended. Goodbye!");
        }
    }

    private void showScreenings() {
        for (Screening screening : service.getScreenings()) {
            output.println(screening.getId() + ". " + screening.getMovieTitle()
                    + " | Standard: " + money(new StandardTicket(screening).getPriceCents())
                    + " | Student: " + money(new StudentTicket(screening).getPriceCents())
                    + " | Available seats: " + screening.getAvailableSeats());
        }
    }

    private void bookTickets() {
        showScreenings();
        int screeningId = readNumber("Screening ID (0 to go back): ");
        if (screeningId == 0) {
            return;
        }
        output.print("Customer name: ");
        String name = input.nextLine().trim();
        int type = readNumber("Ticket type: 1 = standard, 2 = student: ");
        if (type != 1 && type != 2) {
            output.println("Please select a valid ticket type.");
            return;
        }
        int quantity = readNumber("Number of tickets: ");
        try {
            Booking booking = service.bookTickets(screeningId, name, type == 2, quantity);
            output.println("Booked! Booking ID: " + booking.getId()
                    + " | Total: " + money(booking.getTotalPriceCents()));
        } catch (BookingException e) {
            output.println("Booking refused: " + e.getMessage());
        }
    }

    private void cancelBooking() {
        int id = readNumber("Booking ID to cancel (0 to go back): ");
        if (id == 0) {
            return;
        }
        try {
            service.cancelBooking(id);
            output.println("Booking cancelled. Seats restored and revenue refunded.");
        } catch (BookingException e) {
            output.println("Cancellation refused: " + e.getMessage());
        }
    }

    private void showBookings() {
        if (service.getBookings().isEmpty()) {
            output.println("No bookings yet.");
        }
        for (Booking booking : service.getBookings()) {
            String status = "ACTIVE";
            if (booking.isCancelled()) {
                status = "CANCELLED";
            }
            output.println(booking.getId() + ". " + booking.getCustomerName()
                    + " | " + booking.getTicket().getScreening().getMovieTitle()
                    + " | " + booking.getTicket().getType()
                    + " x " + booking.getQuantity()
                    + " | " + money(booking.getTotalPriceCents()) + " | " + status);
        }
        output.println("Revenue after refunds: " + money(service.getRevenueCents()));
    }

    private int readNumber(String prompt) {
        while (true) {
            output.print(prompt);
            try {
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                output.println("Please enter a whole number.");
            }
        }
    }

    private String money(long cents) {
        return String.format(java.util.Locale.ROOT, "%d.%02d EUR", cents / 100, cents % 100);
    }
}
