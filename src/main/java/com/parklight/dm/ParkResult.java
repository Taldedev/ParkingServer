package com.parklight.dm;

import java.io.Serializable;
import java.util.List;

/**
 * Result of parking a vehicle: the issued ticket plus the path the algorithm
 * chose from the entrance to the assigned spot (list of node ids).
 */
public class ParkResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private ParkingTicket ticket;
    private List<String> path;

    public ParkResult() {
    }

    public ParkResult(ParkingTicket ticket, List<String> path) {
        this.ticket = ticket;
        this.path = path;
    }

    public ParkingTicket getTicket() {
        return ticket;
    }

    public List<String> getPath() {
        return path;
    }
}
