package com.parklight.controller;

import com.parklight.server.Request;
import com.parklight.server.Response;

/**
 * A controller handles requests for one area of the application.
 * The server routes each request to a controller based on its action,
 * and the controller runs the matching operation on the services.
 */
public interface IController {

    // Handles a request whose action was already routed to this controller.
    // The full action string is passed so the controller can pick the operation.
    Response<?> handle(String action, Request<?> request);
}
