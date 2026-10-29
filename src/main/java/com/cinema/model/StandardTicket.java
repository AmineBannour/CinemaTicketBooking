package com.cinema.model;

public class StandardTicket extends Ticket {
    public StandardTicket(Screening screening) {
        super(screening);
    }

    @Override
    public int getPriceCents() {
        return getScreening().getBasePriceCents();
    }

    @Override
    public String getType() {
        return "Standard";
    }
}
