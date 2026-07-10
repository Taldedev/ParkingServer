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
        // Entrance into the top corridor, and an expensive direct drop to the bottom.
        addUndirected(algo, ENTRANCE, "J1", 1);
        addUndirected(algo, ENTRANCE, "K1", 5);

        // Top corridor.
        addUndirected(algo, "J1", "J2", 2);
        addUndirected(algo, "J2", "J3", 2);
        addUndirected(algo, "J3", "J4", 2);

        // Bottom corridor.
        addUndirected(algo, "K1", "K2", 2);
        addUndirected(algo, "K2", "K3", 2);
        addUndirected(algo, "K3", "K4", 2);

        // Cheap cross-link at the far end (this is what makes the long way around
        // shorter for some bottom spots).
        addUndirected(algo, "J4", "K4", 1);

        // Each corridor node connects to its spot.
        addUndirected(algo, "J1", "S1", 1);
        addUndirected(algo, "J2", "S2", 1);
        addUndirected(algo, "J3", "S3", 1);
        addUndirected(algo, "J4", "S4", 1);
        addUndirected(algo, "K1", "S5", 1);
        addUndirected(algo, "K2", "S6", 1);
        addUndirected(algo, "K3", "S7", 1);
        addUndirected(algo, "K4", "S8", 1);
    }

    private void addUndirected(IAlgoShortestPath<String> algo, String a, String b, double w) {
        algo.addEdge(a, b, w);
        algo.addEdge(b, a, w);
    }

    private void seedSpotsIfEmpty(ParkingService parking) {
        if (!parking.getAllSpots().isEmpty()) {
            return;
        }
        // Top row spots.
        parking.registerSpot(new ParkingSpot("S1", SpotType.REGULAR, 170, 40));
        parking.registerSpot(new ParkingSpot("S2", SpotType.ELECTRIC, 290, 40));
        parking.registerSpot(new ParkingSpot("S3", SpotType.REGULAR, 410, 40));
        parking.registerSpot(new ParkingSpot("S4", SpotType.DISABLED, 530, 40));
        // Bottom row spots.
        parking.registerSpot(new ParkingSpot("S5", SpotType.REGULAR, 170, 380));
        parking.registerSpot(new ParkingSpot("S6", SpotType.REGULAR, 290, 380));
        parking.registerSpot(new ParkingSpot("S7", SpotType.ELECTRIC, 410, 380));
        parking.registerSpot(new ParkingSpot("S8", SpotType.DISABLED, 530, 380));
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
