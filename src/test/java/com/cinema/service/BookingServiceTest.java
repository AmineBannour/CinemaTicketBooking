package com.cinema.service;

import com.cinema.exception.BookingException;
import com.cinema.model.Booking;
import com.cinema.model.Screening;
import com.cinema.model.StudentTicket;
import com.cinema.model.StandardTicket;
import com.cinema.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {
    private Screening screening;
    private BookingService service;

    @BeforeEach
    void setUp() {
        screening = new Screening(1, "Interstellar", 1000, 5);
        service = new BookingService(List.of(screening));
    }

    @Test
    void standardBookingCalculatesPriceAndReservesSeats() throws BookingException {
        Booking booking = service.bookTickets(1, "Amine", false, 2);

        assertEquals(2000, booking.getTotalPriceCents());
        assertEquals(3, screening.getAvailableSeats());
        assertEquals(2000, service.getRevenueCents());
        assertEquals(1, service.getBookings().size());
    }

    @Test
    void studentBookingAppliesTwentyPercentDiscount() throws BookingException {
        Booking booking = service.bookTickets(1, "Amine", true, 2);

        assertEquals(1600, booking.getTotalPriceCents());
        assertEquals("Student", booking.getTicket().getType());
        assertEquals(3, screening.getAvailableSeats());
    }

    @Test
    void sameTicketReferenceUsesDifferentPriceImplementations() {
        Ticket standard = new StandardTicket(screening);
        Ticket student = new StudentTicket(screening);

        assertEquals(1000, standard.getPriceCents());
        assertEquals(800, student.getPriceCents());
    }

    @Test
    void studentPriceRoundsDownToWholeCents() {
        Ticket ticket = new StudentTicket(new Screening(2, "Film", 999, 5));
        assertEquals(799, ticket.getPriceCents());
    }

    @Test
    void canBookExactlyTheRemainingSeats() throws BookingException {
        service.bookTickets(1, "Amine", false, 5);
        assertEquals(0, screening.getAvailableSeats());
    }

    @Test
    void soldOutScreeningRefusesAnotherBooking() throws BookingException {
        service.bookTickets(1, "Amine", false, 5);

        assertThrows(BookingException.class, () -> service.bookTickets(1, "Sara", true, 1));
        assertEquals(0, screening.getAvailableSeats());
        assertEquals(1, service.getBookings().size());
        assertEquals(5000, service.getRevenueCents());
    }

    @Test
    void excessiveQuantityLeavesStateUnchanged() {
        assertThrows(BookingException.class, () -> service.bookTickets(1, "Amine", false, 6));
        assertEquals(5, screening.getAvailableSeats());
        assertEquals(0, service.getRevenueCents());
        assertTrue(service.getBookings().isEmpty());
    }

    @Test
    void zeroAndNegativeQuantitiesAreRefused() {
        assertThrows(BookingException.class, () -> service.bookTickets(1, "Amine", false, 0));
        assertThrows(BookingException.class, () -> service.bookTickets(1, "Amine", false, -1));
        assertEquals(5, screening.getAvailableSeats());
    }

    @Test
    void missingCustomerNameIsRefused() {
        assertThrows(BookingException.class, () -> service.bookTickets(1, " ", false, 1));
        assertThrows(BookingException.class, () -> service.bookTickets(1, null, false, 1));
        assertEquals(5, screening.getAvailableSeats());
    }

    @Test
    void unknownScreeningIsRefused() {
        assertThrows(BookingException.class, () -> service.bookTickets(99, "Amine", false, 1));
        assertTrue(service.findScreening(99).isEmpty());
        assertTrue(service.getBookings().isEmpty());
    }

    @Test
    void cancellingRestoresSeatsAndRefundsRevenue() throws BookingException {
        Booking booking = service.bookTickets(1, "Amine", true, 2);

        service.cancelBooking(booking.getId());

        assertTrue(booking.isCancelled());
        assertEquals(5, screening.getAvailableSeats());
        assertEquals(0, service.getRevenueCents());
        assertEquals(1, service.getBookings().size()); // Keep cancelled bookings in history.
    }

    @Test
    void bookingCannotBeCancelledTwice() throws BookingException {
        Booking booking = service.bookTickets(1, "Amine", false, 2);
        service.cancelBooking(booking.getId());

        assertThrows(BookingException.class, () -> service.cancelBooking(booking.getId()));
        assertEquals(5, screening.getAvailableSeats());
        assertEquals(0, service.getRevenueCents());
    }

    @Test
    void unknownCancellationDoesNotChangeExistingBooking() throws BookingException {
        service.bookTickets(1, "Amine", false, 2);
        assertThrows(BookingException.class, () -> service.cancelBooking(99));
        assertEquals(3, screening.getAvailableSeats());
        assertEquals(2000, service.getRevenueCents());
    }

    @Test
    void returnedSeatsCanBeBookedAgainWithANewId() throws BookingException {
        Booking first = service.bookTickets(1, "Amine", false, 5);
        service.cancelBooking(first.getId());
        Booking second = service.bookTickets(1, "Sara", true, 5);

        assertEquals(2, second.getId());
        assertEquals(0, screening.getAvailableSeats());
        assertEquals(4000, service.getRevenueCents());
    }

    @Test
    void multipleBookingsHaveIndependentTotals() throws BookingException {
        Booking first = service.bookTickets(1, "Amine", false, 2);
        service.bookTickets(1, "Sara", true, 1);
        assertEquals(2800, service.getRevenueCents());

        service.cancelBooking(first.getId());
        assertEquals(800, service.getRevenueCents());
        assertEquals(4, screening.getAvailableSeats());
    }

    @Test
    void callersCannotRemoveHistoryOrScreenings() {
        assertThrows(UnsupportedOperationException.class, () -> service.getBookings().clear());
        assertThrows(UnsupportedOperationException.class, () -> service.getScreenings().clear());
    }

    @Test
    void duplicateScreeningIdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BookingService(List.of(
                screening, new Screening(1, "Another film", 1200, 10))));
    }

    @Test
    void invalidScreeningDataIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Screening(0, "Film", 1000, 10));
        assertThrows(IllegalArgumentException.class, () -> new Screening(1, "", 1000, 10));
        assertThrows(IllegalArgumentException.class, () -> new Screening(1, "Film", -1, 10));
        assertThrows(IllegalArgumentException.class, () -> new Screening(1, "Film", 1000, 0));
    }

    @Test
    void screeningProtectsSeatCounts() {
        assertThrows(IllegalArgumentException.class, () -> screening.reserveSeats(6));
        assertThrows(IllegalArgumentException.class, () -> screening.reserveSeats(0));
        assertThrows(IllegalArgumentException.class, () -> screening.releaseSeats(1));
        screening.reserveSeats(2);
        screening.releaseSeats(2);
        assertEquals(5, screening.getAvailableSeats());
    }

    @Test
    void directBookingCannotReleaseSeatsTwice() {
        Booking booking = new Booking(1, "Amine", new StandardTicket(screening), 2);
        assertEquals(3, screening.getAvailableSeats());
        booking.cancel();
        assertThrows(IllegalStateException.class, booking::cancel);
        assertEquals(5, screening.getAvailableSeats());
    }
}
