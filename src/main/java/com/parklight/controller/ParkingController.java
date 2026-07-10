package com.parklight.controller;

import com.parklight.dm.ParkingSpot;
import com.parklight.dm.ParkingTicket;
import com.parklight.dm.Vehicle;
import com.parklight.server.Request;
import com.parklight.server.Response;
import com.parklight.service.ParkingService;

import com.google.gson.Gson;

/**
 * Exposes parking operations to the network layer.
 * Translates requests into ParkingService calls and wraps results in responses.
 */
public class ParkingController implements IController {

    private final ParkingService parkingService;
    private final Gson gson = new Gson();

    public ParkingController(ParkingService parkingService) {
        if (parkingService == null) {
            throw new IllegalArgumentException("ParkingService cannot be null");
        }
        this.parkingService = parkingService;
    }

    @Override
    public Response<?> handle(String action, Request<?> request) {
        switch (action) {
            case "parking/park":
                return park(request);
            case "parking/release":
                return release(request);
            case "parking/spots":
                return Response.ok(parkingService.getAllSpots());
            case "parking/available":
                return Response.ok(parkingService.getAvailableSpots());
            case "parking/occupied":
                return Response.ok(parkingService.getOccupiedSpots());
            default:
                return Response.error("Unknown parking action: " + action);
        }
    }

    // Body is expected to be a Vehicle (as a JSON object).
    private Response<?> park(Request<?> request) {
        Vehicle vehicle = gson.fromJson(gson.toJson(request.getBody()), Vehicle.class);
        if (vehicle == null || vehicle.getLicensePlate() == null || vehicle.getType() == null) {
            return Response.error("Invalid vehicle in request body");
        }
        ParkingTicket ticket = parkingService.parkVehicle(vehicle);
        if (ticket == null) {
            return Response.error("No available compatible spot");
        }
        return Response.ok(ticket);
    }

    // Body is expected to carry the ticket id under the "ticketId" field.
    private Response<?> release(Request<?> request) {
        TicketIdBody payload =
                gson.fromJson(gson.toJson(request.getBody()), TicketIdBody.class);
        if (payload == null || payload.ticketId == null) {
            return Response.error("Missing ticketId in request body");
        }
        ParkingTicket ticket = parkingService.releaseVehicle(payload.ticketId);
        if (ticket == null) {
            return Response.error("Ticket not found or already closed");
        }
        return Response.ok(ticket);
    }

    // Small helper to read the ticketId field out of the JSON body.
    private static class TicketIdBody {
        String ticketId;
    }
}
