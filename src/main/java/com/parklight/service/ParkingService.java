package com.parklight.service;

import com.parklight.algorithm.IAlgoShortestPath;
import com.parklight.dao.IDao;
import com.parklight.dm.ParkingSpot;
import com.parklight.dm.ParkingTicket;
import com.parklight.dm.SpotType;
import com.parklight.dm.Vehicle;
import com.parklight.dm.VehicleType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Business logic for the parking lot.
 * Allocates the closest available compatible spot for each incoming vehicle
 * using an injected shortest-path algorithm.
 */
public class ParkingService {

    private final IAlgoShortestPath<String> algorithm;
    private final IDao<String, ParkingSpot> spotsDao;
    private final IDao<String, ParkingTicket> ticketsDao;
    private final BillingService billing;
    private final String entranceNodeId;

    public ParkingService(IAlgoShortestPath<String> algorithm,
                          IDao<String, ParkingSpot> spotsDao,
                          IDao<String, ParkingTicket> ticketsDao,
                          BillingService billing,
                          String entranceNodeId) {
        if (algorithm == null || spotsDao == null || ticketsDao == null
                || billing == null || entranceNodeId == null) {
            throw new IllegalArgumentException("All dependencies must be non-null");
        }
        this.algorithm = algorithm;
        this.spotsDao = spotsDao;
        this.ticketsDao = ticketsDao;
        this.billing = billing;
        this.entranceNodeId = entranceNodeId;
    }

    // Parks the vehicle in the closest available compatible spot.
    // Returns the issued ticket, or null if no spot is available or reachable.
    public ParkingTicket parkVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        List<ParkingSpot> candidates = new ArrayList<>();
        for (ParkingSpot s : spotsDao.getAll()) {
            if (!s.isOccupied() && isCompatible(s.getType(), vehicle.getType())) {
                candidates.add(s);
            }
        }
        ParkingSpot chosen = findClosest(candidates);
        if (chosen == null) {
            return null;
        }
        chosen.setOccupied(true);
        spotsDao.save(chosen.getId(), chosen);

        String ticketId = UUID.randomUUID().toString();
        ParkingTicket ticket = new ParkingTicket(
                ticketId, vehicle, chosen, System.currentTimeMillis());
        ticketsDao.save(ticketId, ticket);
        return ticket;
    }

    // Releases the spot held by this ticket, computes the price, and saves both.
    // Returns the updated ticket, or null if ticket is unknown or already closed.
    public ParkingTicket releaseVehicle(String ticketId) {
        ParkingTicket ticket = ticketsDao.get(ticketId);
        if (ticket == null || ticket.getExitTime() > 0) {
            return null;
        }
        ParkingSpot spot = ticket.getSpot();
        spot.setOccupied(false);
        spotsDao.save(spot.getId(), spot);

        ticket.setExitTime(System.currentTimeMillis());
        ticket.setPrice(billing.calculatePrice(ticket));
        ticketsDao.save(ticketId, ticket);
        return ticket;
    }

    // ----- Spot management (save / get / delete / retrieve) -----

    public void registerSpot(ParkingSpot spot) {
        if (spot == null) {
            throw new IllegalArgumentException("Spot cannot be null");
        }
        spotsDao.save(spot.getId(), spot);
    }

    public ParkingSpot getSpot(String spotId) {
        return spotsDao.get(spotId);
    }

    public boolean deleteSpot(String spotId) {
        return spotsDao.delete(spotId);
    }

    public List<ParkingSpot> getAllSpots() {
        return spotsDao.getAll();
    }

    public List<ParkingSpot> getAvailableSpots() {
        List<ParkingSpot> result = new ArrayList<>();
        for (ParkingSpot s : spotsDao.getAll()) {
            if (!s.isOccupied()) {
                result.add(s);
            }
        }
        return result;
    }

    public List<ParkingSpot> getOccupiedSpots() {
        List<ParkingSpot> result = new ArrayList<>();
        for (ParkingSpot s : spotsDao.getAll()) {
            if (s.isOccupied()) {
                result.add(s);
            }
        }
        return result;
    }

    // ----- Helpers -----

    // Returns the candidate spot with the shortest path from the entrance, or null
    // if none of the candidates are reachable.
    private ParkingSpot findClosest(List<ParkingSpot> candidates) {
        ParkingSpot best = null;
        double bestDist = Double.POSITIVE_INFINITY;
        for (ParkingSpot s : candidates) {
            double d = algorithm.getDistance(entranceNodeId, s.getId());
            if (d < bestDist) {
                bestDist = d;
                best = s;
            }
        }
        return best;
    }

    // Strict 1:1 mapping between vehicle type and spot type.
    private boolean isCompatible(SpotType spotType, VehicleType vehicleType) {
        switch (vehicleType) {
            case REGULAR:  return spotType == SpotType.REGULAR;
            case DISABLED: return spotType == SpotType.DISABLED;
            case ELECTRIC: return spotType == SpotType.ELECTRIC;
            default:       return false;
        }
    }
}
