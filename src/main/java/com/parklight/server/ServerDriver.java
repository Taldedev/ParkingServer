package com.parklight.server;

/**
 * Entry point for the parking server.
 */
public class ServerDriver {

    public static void main(String[] args) {
        Server server = new Server(34567);
        new Thread(server).start();
    }
}
