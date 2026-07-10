package com.parklight.server;

import com.parklight.algorithm.DijkstraAlgoImpl;
import com.parklight.algorithm.IAlgoShortestPath;
import com.parklight.controller.ControllerFactory;
import com.parklight.dao.DaoFileImpl;
import com.parklight.dao.IDao;
import com.parklight.dm.ParkingSpot;
import com.parklight.dm.ParkingTicket;
import com.parklight.dm.SpotType;
import com.parklight.service.BillingService;
import com.parklight.service.ParkingService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Parking server. Listens on a TCP port and serves parking requests.
 * Wires the parking service (with the Dijkstra algorithm and a billing helper)
 * and accepts client connections, handling each on its own thread.
 */
public class Server implements Runnable {

    private static final String SPOTS_FILE = "spots.dat";
    private static final String TICKETS_FILE = "tickets.dat";
    private static final String ENTRANCE = "ENTRANCE";

    private final int port;
    private ControllerFactory factory;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        initComponents();
        listen();
    }

    private void initComponents() {
        IDao<String, ParkingSpot> spotsDao = new DaoFileImpl<>(SPOTS_FILE);
        IDao<String, ParkingTicket> ticketsDao = new DaoFileImpl<>(TICKETS_FILE);

        IAlgoShortestPath<String> algo = new DijkstraAlgoImpl<>();
        buildLotGraph(algo);

        BillingService billing = new BillingService(ticketsDao);
        ParkingService parking =
                new ParkingService(algo, spotsDao, ticketsDao, billing, ENTRANCE);

        seedSpotsIfEmpty(parking);

        this.factory = new ControllerFactory(parking);
    }

    private void buildLotGraph(IAlgoShortestPath<String> algo) {
        addUndirected(algo, ENTRANCE, "AISLE", 1);
        addUndirected(algo, "AISLE", "S1", 1);
        addUndirected(algo, "AISLE", "S2", 2);
        addUndirected(algo, "AISLE", "S3", 3);
        addUndirected(algo, "AISLE", "S4", 4);
    }

    private void addUndirected(IAlgoShortestPath<String> algo, String a, String b, double w) {
        algo.addEdge(a, b, w);
        algo.addEdge(b, a, w);
    }

    private void seedSpotsIfEmpty(ParkingService parking) {
        if (!parking.getAllSpots().isEmpty()) {
            return;
        }
        parking.registerSpot(new ParkingSpot("S1", SpotType.REGULAR, 1, 0));
        parking.registerSpot(new ParkingSpot("S2", SpotType.REGULAR, 2, 0));
        parking.registerSpot(new ParkingSpot("S3", SpotType.ELECTRIC, 3, 0));
        parking.registerSpot(new ParkingSpot("S4", SpotType.DISABLED, 4, 0));
    }

    private void listen() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ParkLight parking server listening on port " + port);
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(new HandleRequest(client, factory)).start();
            }
        } catch (IOException e) {
            System.err.println("Parking server failed on port " + port + ": " + e.getMessage());
        }
    }
}
