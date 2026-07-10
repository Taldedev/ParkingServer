package com.parklight.dm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A serializable snapshot of the parking-lot graph for the client to draw.
 * Contains nodes (with positions and optional spot info) and weighted edges.
 */
public class GraphInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    /**
     * A graph node. For spot nodes, type and occupied are set; for structural
     * nodes (entrance, aisle) they stay null/false and spot is false.
     */
    public static class Node implements Serializable {

        private static final long serialVersionUID = 1L;

        private String id;
        private double x;
        private double y;
        private boolean spot;
        private String type;      // REGULAR / DISABLED / ELECTRIC, or null
        private boolean occupied;

        public Node() {
        }

        public Node(String id, double x, double y, boolean spot, String type, boolean occupied) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.spot = spot;
            this.type = type;
            this.occupied = occupied;
        }

        public String getId() {
            return id;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public boolean isSpot() {
            return spot;
        }

        public String getType() {
            return type;
        }

        public boolean isOccupied() {
            return occupied;
        }
    }

    /**
     * A weighted undirected edge between two node ids.
     */
    public static class Edge implements Serializable {

        private static final long serialVersionUID = 1L;

        private String from;
        private String to;
        private double weight;

        public Edge() {
        }

        public Edge(String from, String to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }

        public double getWeight() {
            return weight;
        }
    }
}
