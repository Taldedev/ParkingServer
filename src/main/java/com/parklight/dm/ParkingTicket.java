package com.parklight.dm;

import java.util.Objects;

/**
 * A ticket issued when a vehicle parks. Tracks entry/exit times and price.
 * exitTime = 0 means the vehicle is still parked.
 */
public class ParkingTicket implements DataModel {

    private static final long serialVersionUID = 1L;

    private String ticketId;
    private Vehicle vehicle;
    private ParkingSpot spot;
    private long entryTime;   // epoch millis
    private long exitTime;    // epoch millis, 0 while still parked
    private double price;     // computed at checkout

    public ParkingTicket() {
    }

    public ParkingTicket(String ticketId, Vehicle vehicle, ParkingSpot spot, long entryTime) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = entryTime;
        this.exitTime = 0;
        this.price = 0;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public ParkingSpot getSpot() {
        return spot;
    }

    public void setSpot(ParkingSpot spot) {
        this.spot = spot;
    }

    public long getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }

    public long getExitTime() {
        return exitTime;
    }

    public void setExitTime(long exitTime) {
        this.exitTime = exitTime;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingTicket)) return false;
        ParkingTicket other = (ParkingTicket) o;
        return Objects.equals(ticketId, other.ticketId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId);
    }

    @Override
    public String toString() {
        return "ParkingTicket{id='" + ticketId + "', vehicle=" + vehicle +
               ", spot=" + spot + ", entry=" + entryTime + ", exit=" + exitTime +
               ", price=" + price + '}';
    }
}
