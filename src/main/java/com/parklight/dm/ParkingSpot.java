package com.parklight.dm;

import java.util.Objects;

/**
 * A single parking spot in the lot.
 * Coordinates (x, y) are used by the A* heuristic to estimate distances.
 */
public class ParkingSpot implements DataModel {

    private static final long serialVersionUID = 1L;

    private String id;
    private SpotType type;
    private double x;
    private double y;
    private boolean occupied;

    public ParkingSpot() {
    }

    public ParkingSpot(String id, SpotType type, double x, double y) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.occupied = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SpotType getType() {
        return type;
    }

    public void setType(SpotType type) {
        this.type = type;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingSpot)) return false;
        ParkingSpot other = (ParkingSpot) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ParkingSpot{id='" + id + "', type=" + type +
               ", x=" + x + ", y=" + y + ", occupied=" + occupied + '}';
    }
}
