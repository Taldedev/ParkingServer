package com.parklight.dm;

import java.util.Objects;

/**
 * A vehicle that wants to park in the lot.
 */
public class Vehicle implements DataModel {

    private static final long serialVersionUID = 1L;

    private String licensePlate;
    private VehicleType type;

    public Vehicle() {
    }

    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public VehicleType getType() {
        return type;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        Vehicle other = (Vehicle) o;
        return Objects.equals(licensePlate, other.licensePlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licensePlate);
    }

    @Override
    public String toString() {
        return "Vehicle{plate='" + licensePlate + "', type=" + type + '}';
    }
}
