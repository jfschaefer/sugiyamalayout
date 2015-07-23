package de.jfschaefer.sugiyamalayout;

import java.util.*;


public class DiGraph<V, E> {
    private Hashtable<V, Node> vToNode = new Hashtable<V, Node>();
    private Hashtable<E, Edge> eToEdge = new Hashtable<E, Edge>();

    public DiGraph() {

    }

    public void addNode(V node, double width, double height) {
        Node newNode = new Node(width, height, node.toString());
        vToNode.put(node, newNode);
    }

    public void addEdge(E edge, V start, V end) {
        assert (start != end);        //haven't figured out how to deal with loops yet
        Edge newEdge = new Edge(vToNode.get(start), vToNode.get(end));
        eToEdge.put(edge, newEdge);
    }

    public Layout<V, E> generateLayout(Configuration config) {
        reset();
        removeCycles();

        LGraph lg;
        if (config.getUseAlternativeAlgorithm()) {
            PGraph pg = new PGraph(config);
            for (Node n : vToNode.values()) {
                pg.addNode(n);
            }
            for (Edge e : eToEdge.values()) {
                pg.addEdge(e);
            }
            lg = pg.getLGraph();
            // CHEATING:
            while (lg.topDownGreedySwapping()) {
                System.err.println("CHEATING IN DIGRAPH");
            }
        } else {
            setLayers();
            lg = runSugiyama(config);
        }

        lg.setInitialPixelOffsets();
        for (int i = 0; i < 5; i++) {
            lg.topDownOffsetRelaxation();
            lg.bottomUpOffsetRelaxation();
        }
        lg.topDownOffsetRelaxation();
        return new Layout<V, E>(lg, vToNode, eToEdge, config);
    }

    private LGraph runSugiyama(Configuration config) {
        LGraph lg = generateLGraph(config);
        lg.topDownParentMedianReordering();
        lg.bottomUpMedianReordering();
        lg.topDownAMReordering();
        while (lg.topDownGreedySwapping());
        return lg;
    }

    private void removeCycles() {
        // Reverse as few edges as possible to get an acyclic graph
        // Due to the complexity of the problem (I think it's NP-complete), use a greedy (non-optimal) approach
        // Collection<Node> remainingNodes = vToNode.values();
        Set<Node> remainingNodes = new HashSet<Node>();
        for (Node n : vToNode.values()) {
            remainingNodes.add(n);
        }

        while (!remainingNodes.isEmpty()) {
            int size;
            // remove remaining sources and sinks
            do {
                size = remainingNodes.size();
                Set<Node> done = new HashSet<Node>();
                for (Node node : remainingNodes) {
                    if (node.isSinkIn(remainingNodes) || node.isSourceIn(remainingNodes)) {
                        done.add(node);
                    }
                }
                remainingNodes.removeAll(done);
            } while (size != remainingNodes.size());

            // If nodes are left, reverse a few edges to make one node as sink or a source
            if (!remainingNodes.isEmpty()) {
                double bestRatio = 1d;  // higher than upper bound
                Node chosenNode = null;
                boolean inHigherThanOutDegree = false;
                for (Node node : remainingNodes) {
                    int indegree = node.inDegreeIn(remainingNodes);
                    int outdegree = node.outDegree(remainingNodes);
                    assert indegree != 0;
                    assert outdegree != 0;
                    double ratio = Math.min((double)indegree, (double)outdegree) / (indegree + outdegree);
                    if (ratio < bestRatio) {
                        bestRatio = ratio;
                        chosenNode = node;
                        inHigherThanOutDegree = (indegree > outdegree);
                    }
                }
                assert chosenNode != null;
                Set<Edge> toBeReversed = new HashSet<Edge>();
                if (inHigherThanOutDegree) {
                    for (Edge e : chosenNode.getOutgoingEdges()) {
                        if (remainingNodes.contains(e.getEnd())) {
                            toBeReversed.add(e);
                        }
                    }
                } else {
                    for (Edge e : chosenNode.getIngoingEdges()) {
                        if (remainingNodes.contains(e.getStart())) {
                            toBeReversed.add(e);
                        }
                    }
                }
                for (Edge e : toBeReversed) {
                    e.reverse();
                }
            }
        }
    }

    void reset() {
        for (Node node : vToNode.values()) {
            node.reset();
        }
        for (Edge edge : eToEdge.values()) {
            edge.reset();
        }
    }

    private void setLayers() {
        // lay out nodes top-down
        Set<Node> sources = new HashSet<Node>();
        for (Node n : vToNode.values()) {
            if (n.isSource()) {   //Roots should be in layer 0
                sources.add(n);
                n.setLayer(0);
            }
        }
        for (Node source : sources) {
            setLayersTopDownDFS(source);
        }

        // Collection<Node> toBeChecked = vToNode.values();
        Set<Node> toBeChecked = new HashSet<Node>();
        for (Node n : vToNode.values()) {
            toBeChecked.add(n);
        }

        while (!toBeChecked.isEmpty()) {
            Set<Node> toBeCheckedNextTime = new HashSet<Node>();
            for (Node n : toBeChecked) {
                if (!n.getOutgoingEdges().isEmpty()) {
                    int minChildLayer = -1;  //not yet set
                    for (Edge e : n.getOutgoingEdges()) {
                        if (minChildLayer == -1 || minChildLayer > e.getEnd().getLayer()) {
                            minChildLayer = e.getEnd().getLayer();
                        }
                    }
                    if (minChildLayer - 1 > n.getLayer()) {   //we can pull n down
                        for (Edge e : n.getIngoingEdges()) {    //parents may be moved down in the next iteration
                            toBeCheckedNextTime.add(e.getStart());
                        }
                        n.setLayer(minChildLayer - 1);
                    }
                }
            }
            toBeChecked = toBeCheckedNextTime;
        }
    }

    private void setLayersTopDownDFS(Node parent) {
        for (Edge e : parent.getOutgoingEdges()) {
            Node child = e.getEnd();
            if (!child.layerIsSet() || child.getLayer() <= parent.getLayer()) {
                child.setLayer(parent.getLayer() + 1);
                setLayersTopDownDFS(child);
            }
        }
    }

    LGraph generateLGraph(Configuration config) {
        LGraph lg = new LGraph(config);
        for (Node node : vToNode.values()) {
            lg.insertNode(node.getLNode());
        }
        for (Edge edge : eToEdge.values()) {
            for (LNode lnode : edge.getDummyLNodes()) {
                lg.insertNode(lnode);
            }
        }
        return lg;
    }
}
