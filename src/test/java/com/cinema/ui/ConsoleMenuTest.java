package com.cinema.ui;

import com.cinema.model.Screening;
import com.cinema.service.BookingService;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class ConsoleMenuTest {
    private final Screening screening = new Screening(1, "Film", 1000, 5);
    private final BookingService service = new BookingService(List.of(screening));

    private String runMenu(String commands) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(bytes, true, StandardCharsets.UTF_8);
        new ConsoleMenu(service, new Scanner(commands), output).run();
        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void canBookViewAndCancelTickets() {
        String text = runMenu("1\n2\n1\nAmine\n2\n2\n4\n3\n1\n4\n5\n");
        assertTrue(text.contains("Total: 16.00 EUR"));
        assertTrue(text.contains("ACTIVE"));
        assertTrue(text.contains("CANCELLED"));
        assertEquals(5, screening.getAvailableSeats());
        assertEquals(0, service.getRevenueCents());
    }

    @Test
    void invalidNumbersDoNotCrashTheMenu() {
        String text = runMenu("abc\n999999999999999\n9\n5\n");
        assertTrue(text.contains("Please enter a whole number"));
        assertTrue(text.contains("Please choose a number from 1 to 5"));
        assertTrue(text.contains("Goodbye"));
    }

    @Test
    void inputEndingDuringBookingLeavesStateUnchanged() {
        String text = runMenu("2\n1\nAmine\n");
        assertTrue(text.contains("Input ended"));
        assertEquals(5, screening.getAvailableSeats());
        assertTrue(service.getBookings().isEmpty());
    }

    @Test
    void tooManySeatsShowsHelpfulError() {
        String text = runMenu("2\n1\nAmine\n1\n6\n5\n");
        assertTrue(text.contains("Only 5 seats are available"));
        assertTrue(service.getBookings().isEmpty());
    }

    @Test
    void goingBackAndInvalidTicketTypeDoNotBook() {
        runMenu("2\n0\n2\n1\nAmine\n3\n5\n");
        assertEquals(5, screening.getAvailableSeats());
        assertTrue(service.getBookings().isEmpty());
    }
}
