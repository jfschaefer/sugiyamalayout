package de.jfschaefer.sugiyamalayout;

import java.util.Collection;
import java.util.HashSet;

class Node {
    private double width, height;
    private LNode lnode = null;
    private HashSet<Edge> ingoingEdges = new HashSet<Edge>();
    private HashSet<Edge> outgoingEdges = new HashSet<Edge>();
    private int layer = -1;

    Node(double width, double height) {
        this.width = width;
        this.height = height;
    }

    double getWidth() {
        return width;
    }

    double getHeight() {
        return height;
    }

    LNode getLNode() {
        if (lnode == null) {
            lnode = new LNode(this, layer);
        }
        return lnode;
    }

    void addIngoingEdge(Edge e) {
        assert e.getEnd() == this;
        ingoingEdges.add(e);
    }

    void addOutgoingEdge(Edge e) {
        assert e.getStart() == this;
        outgoingEdges.add(e);
    }

    void onEdgeReverse(Edge e) {
        if (ingoingEdges.contains(e)) {
            ingoingEdges.remove(e);
            outgoingEdges.add(e);
        } else {
            assert outgoingEdges.contains(e);
            outgoingEdges.remove(e);
            ingoingEdges.add(e);
        }
    }

    boolean isSinkIn(Collection<Node> subgraph) {
        for (Edge e : outgoingEdges) {
            if (subgraph.contains(e.getEnd())) return false;
        }
        return true;
    }

    boolean isSourceIn(Collection<Node> subgraph) {
        for (Edge e : ingoingEdges) {
            if (subgraph.contains(e.getStart())) return false;
        }
        return true;
    }

    boolean isSink() {
        return outgoingEdges.isEmpty();
    }

    boolean isSource() {
        return ingoingEdges.isEmpty();
    }

    int outDegree(Collection<Node> subgraph) {
        int degree = 0;
        for (Edge e : outgoingEdges) {
            if (subgraph.contains(e.getEnd())) degree++;
        }
        return degree;
    }

    int inDegreeIn(Collection<Node> subgraph) {
        int degree = 0;
        for (Edge e : ingoingEdges) {
            if (subgraph.contains(e.getStart())) degree++;
        }
        return degree;
    }

    Collection<Edge> getIngoingEdges() {
        return ingoingEdges;
    }

    Collection<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }

    void setLayer(int layer) {
        this.layer = layer;
    }

    boolean layerIsSet() {
        return layer != -1;
    }

    int getLayer() {
        return layer;
    }

    void reset() {
        layer = -1;
        lnode = null;
    }
}
