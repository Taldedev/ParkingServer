package com.parklight.service;

import com.parklight.dao.IDao;
import com.parklight.dm.ParkingTicket;
import com.parklight.dm.SpotType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates parking fees and provides revenue reports.
 * Pricing is per-hour and varies by spot type. Partial hours round up,
 * minimum charge is one hour.
 */
public class BillingService {

    private static final long HOUR_MS = 60L * 60L * 1000L;
    private static final double DEFAULT_PRICE = 10.0;

    private final IDao<String, ParkingTicket> ticketsDao;
    private final Map<SpotType, Double> pricePerHour;

    public BillingService(IDao<String, ParkingTicket> ticketsDao) {
        if (ticketsDao == null) {
            throw new IllegalArgumentException("Tickets DAO cannot be null");
        }
        this.ticketsDao = ticketsDao;
        this.pricePerHour = new EnumMap<>(SpotType.class);
        this.pricePerHour.put(SpotType.REGULAR, 10.0);
        this.pricePerHour.put(SpotType.DISABLED, 5.0);
        this.pricePerHour.put(SpotType.ELECTRIC, 15.0);
    }

    public void setPricePerHour(SpotType type, double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        pricePerHour.put(type, price);
    }

    // Computes the fee for a checked-out ticket.
    public double calculatePrice(ParkingTicket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }
        if (ticket.getExitTime() <= 0) {
            throw new IllegalStateException("Ticket has not been checked out yet");
        }
        long durationMs = ticket.getExitTime() - ticket.getEntryTime();
        // Ceil to whole hours, minimum 1 hour.
        long hours = Math.max(1, (durationMs + HOUR_MS - 1) / HOUR_MS);
        double rate = pricePerHour.getOrDefault(ticket.getSpot().getType(), DEFAULT_PRICE);
        return hours * rate;
    }

    // Sum of `price` across all closed tickets.
    public double getTotalRevenue() {
        double total = 0;
        for (ParkingTicket t : ticketsDao.getAll()) {
            if (t.getExitTime() > 0) {
                total += t.getPrice();
            }
        }
        return total;
    }

    // CRUD pass-throughs over tickets, satisfying the per-service save/delete/retrieve requirement.

    public void saveTicket(ParkingTicket ticket) {
        ticketsDao.save(ticket.getTicketId(), ticket);
    }

    public ParkingTicket getTicket(String ticketId) {
        return ticketsDao.get(ticketId);
    }

    public boolean deleteTicket(String ticketId) {
        return ticketsDao.delete(ticketId);
    }

    public List<ParkingTicket> getAllTickets() {
        return ticketsDao.getAll();
    }
}
