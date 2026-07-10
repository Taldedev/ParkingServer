package com.parklight.controller;

import com.parklight.service.ParkingService;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps an action prefix to the controller that handles it.
 * This server exposes only parking operations, so it registers a single
 * controller. Adding a controller means adding one entry here.
 */
public class ControllerFactory {

    private final Map<String, IController> controllers = new HashMap<>();

    public ControllerFactory(ParkingService parkingService) {
        controllers.put("parking", new ParkingController(parkingService));
    }

    // Returns the controller for an action like "parking/park", or null if unknown.
    public IController getController(String action) {
        if (action == null) {
            return null;
        }
        int slash = action.indexOf('/');
        String prefix = slash < 0 ? action : action.substring(0, slash);
        return controllers.get(prefix);
    }
}
