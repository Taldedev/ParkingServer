package com.parklight.server;

import com.parklight.controller.ControllerFactory;
import com.parklight.controller.IController;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Handles a single client connection on its own thread.
 * Reads one JSON request from the socket, routes it to the matching
 * controller through the factory, and writes back a JSON response.
 */
public class HandleRequest implements Runnable {

    private final Socket socket;
    private final ControllerFactory factory;
    private final Gson gson = new Gson();

    public HandleRequest(Socket socket, ControllerFactory factory) {
        this.socket = socket;
        this.factory = factory;
    }

    @Override
    public void run() {
        try (Scanner reader = new Scanner(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String requestJson = readRequest(reader);
            Response<?> response = process(requestJson);

            writer.println(gson.toJson(response));
            writer.flush();
        } catch (IOException e) {
            // Nothing more we can do for this connection; log and move on.
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            closeSocket();
        }
    }

    // Reads the request as a single line of JSON.
    private String readRequest(Scanner reader) {
        if (reader.hasNextLine()) {
            return reader.nextLine();
        }
        return "";
    }

    // Parses the JSON, finds the controller, and runs the action.
    private Response<?> process(String requestJson) {
        if (requestJson == null || requestJson.trim().isEmpty()) {
            return Response.error("Empty request");
        }
        Request<?> request;
        try {
            request = gson.fromJson(requestJson, Request.class);
        } catch (Exception e) {
            return Response.error("Malformed JSON request");
        }
        if (request == null) {
            return Response.error("Empty request");
        }

        String action = request.getHeader("action");
        if (action == null) {
            return Response.error("Missing action header");
        }

        IController controller = factory.getController(action);
        if (controller == null) {
            return Response.error("No controller for action: " + action);
        }
        return controller.handle(action, request);
    }

    private void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Failed to close socket: " + e.getMessage());
        }
    }
}
