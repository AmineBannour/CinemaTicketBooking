package com.cinema.model;

public class StudentTicket extends Ticket {
    public StudentTicket(Screening screening) {
        super(screening);
    }

    @Override
    public int getPriceCents() {
        // Students pay 80% of the normal price, rounded down to a whole cent.
        return (int) (getScreening().getBasePriceCents() * 80L / 100);
    }

    @Override
    public String getType() {
        return "Student";
    }
}
