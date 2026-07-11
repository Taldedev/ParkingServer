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

    // Builds a fresh snapshot of the lot graph with CURRENT spot occupancy.
    public com.parklight.dm.GraphInfo getGraphInfo() {
        com.parklight.dm.GraphInfo g = new com.parklight.dm.GraphInfo();

        // Structural nodes (entrance + two corridors) with hand-placed positions.
        g.addNode(new com.parklight.dm.GraphInfo.Node("ENTRANCE", 40, 235, false, null, false));
        g.addNode(new com.parklight.dm.GraphInfo.Node("J1", 110, 130, false, null, false));
        g.addNode(new com.parklight.dm.GraphInfo.Node("J2", 210, 130, false, null, false));
        g.addNode(new com.parklight.dm.GraphInfo.Node("J3", 310, 130, false, null, false));
        g.addNode(new com.parklight.dm.GraphInfo.Node("J4", 410, 130, false, null, false));
        g.addNode(new com.parklight.dm.GraphInfo.Node("K1", 110, 330, false, null, false));
        g.addNode(new com.parklight.dm.GraphInfo.Node("K2", 210, 330, false, null, false));
        g.addNode(new com.parklight.dm.GraphInfo.Node("K3", 310, 330, false, null, false));
        g.addNode(new com.parklight.dm.GraphInfo.Node("K4", 410, 330, false, null, false));

        // Spot nodes with live type/occupied, sorted by id for a tidy map.
        java.util.List<com.parklight.dm.ParkingSpot> sortedSpots =
                new java.util.ArrayList<>(getAllSpots());
        sortedSpots.sort(java.util.Comparator.comparing(com.parklight.dm.ParkingSpot::getId));
        for (com.parklight.dm.ParkingSpot s : sortedSpots) {
            g.addNode(new com.parklight.dm.GraphInfo.Node(
                    s.getId(), s.getX(), s.getY(), true,
                    s.getType() == null ? null : s.getType().name(),
                    s.isOccupied()));
        }

        // Edges (undirected, listed once) matching buildLotGraph.
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("ENTRANCE", "J1", 1));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("ENTRANCE", "K1", 5));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("J1", "J2", 2));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("J2", "J3", 2));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("J3", "J4", 2));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("K1", "K2", 2));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("K2", "K3", 2));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("K3", "K4", 2));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("J4", "K4", 1));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("J1", "S1", 1));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("J2", "S2", 1));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("J3", "S3", 1));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("J4", "S4", 1));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("K1", "S5", 1));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("K2", "S6", 1));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("K3", "S7", 1));
        g.addEdge(new com.parklight.dm.GraphInfo.Edge("K4", "S8", 1));

        return g;
    }

    // Parks the vehicle in the closest available compatible spot.
    // Returns the issued ticket, or null if no spot is available or reachable.
    public ParkingTicket parkVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null");
        }
        if (isPlateParked(vehicle.getLicensePlate())) {
            return null; // already parked - do not issue a second ticket
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

    // Parks a vehicle and also returns the path the algorithm chose from the
    // entrance to the assigned spot (for the map view).
    public com.parklight.dm.ParkResult parkVehicleWithPath(Vehicle vehicle) {
        ParkingTicket ticket = parkVehicle(vehicle);
        if (ticket == null) {
            return null;
        }
        java.util.List<String> path =
                algorithm.findShortestPath(entranceNodeId, ticket.getSpot().getId());
        return new com.parklight.dm.ParkResult(ticket, path);
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

    // Returns tickets for vehicles still parked (not yet released).
    public List<ParkingTicket> getActiveTickets() {
        List<ParkingTicket> active = new ArrayList<>();
        for (ParkingTicket t : ticketsDao.getAll()) {
            if (t.getExitTime() == 0) {
                active.add(t);
            }
        }
        return active;
    }

    // True if a vehicle with this plate already has an open (not released) ticket.
    public boolean isPlateParked(String licensePlate) {
        if (licensePlate == null) {
            return false;
        }
        for (ParkingTicket t : getActiveTickets()) {
            if (t.getVehicle() != null
                    && licensePlate.equals(t.getVehicle().getLicensePlate())) {
                return true;
            }
        }
        return false;
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
