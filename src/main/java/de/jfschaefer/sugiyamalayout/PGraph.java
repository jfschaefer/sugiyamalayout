package de.jfschaefer.sugiyamalayout;

import java.util.*;

/*
    A PGraph is a graph representation that supports my personal layered graph algorithm,
    which will hopefully provide good results for small graphs where most vertices have an indegree of at most 1.
 */

public class PGraph {
    Set<Edge> edgeSet = new HashSet<Edge>();
    Map<Node, PNode> nodeMap = new HashMap<Node, PNode>();
    PNode root;    // A dummy node, which is the parent of all the sources
    Configuration config;

    public PGraph(Configuration configuration) {
        config = configuration;
    }

    public void addNode(Node n) {
        nodeMap.put(n, new PNode(n));
    }

    public void addEdge(Edge e) {
        PNode parent = nodeMap.get(e.getStart());
        PNode child = nodeMap.get(e.getEnd());
        child.addParent(parent);
        parent.addChild(new PChild(child, false, e));
        edgeSet.add(e);
    }

    public LGraph getLGraph() {
        root = new PNode(null);
        root.setRoot();
        for (PNode source : getSources()) {
            root.addChild(new PChild(source, true, null));
            source.addParent(root);
        }

        while (root.tryToResolve());

        for (PNode node : nodeMap.values()) {
            if (node.isResolved() && !node.isRoot()) {
                node.getOriginalNode().setMarker(true);
            }
        }

        setLayers();
        LGraph lg = new LGraph(config);
        fillLGraphDFS(lg, root);
        return lg;
    }

    private void setLayers() {
        Iterator<PNode> sourceIter = root.getChildPNodeIterator();
        while (sourceIter.hasNext()) {
            PNode source = sourceIter.next();
            source.getOriginalNode().setLayer(0);
            setLayersDFS(source);
        }
        // and pull down nodes if possible
        Set<PNode> toBeChecked = new HashSet<PNode>();
        for (PNode node : nodeMap.values()) {
            toBeChecked.add(node);
        }
        while (!toBeChecked.isEmpty()) {
            Set<PNode> toBeCheckedNext = new HashSet<PNode>();
            for (PNode node : toBeChecked) {
                if (node.isRoot()) continue;

                int minLayer = -1;  // not set
                Iterator<PNode> it = node.getChildPNodeIterator();
                while (it.hasNext()) {
                    PNode child = it.next();
                    if (minLayer == -1 || child.getOriginalNode().getLayer() < minLayer) {
                        minLayer = child.getOriginalNode().getLayer();
                    }
                }
                if (minLayer - 1 > node.getOriginalNode().getLayer()) {
                    node.getOriginalNode().setLayer(minLayer - 1);
                    for (PNode parent : node.getParents()) {
                        toBeCheckedNext.add(parent);
                    }
                }
            }
            toBeChecked = toBeCheckedNext;
        }
    }

    private void setLayersDFS(PNode root) {
        Iterator<PNode> it = root.getChildPNodeIterator();
        while (it.hasNext()) {
            PNode child = it.next();
            if (child.getOriginalNode().getLayer() <= root.getOriginalNode().getLayer()) {
                child.getOriginalNode().setLayer(root.getOriginalNode().getLayer() + 1);
                setLayersDFS(child);
            }
        }
    }

    private Set<PNode> getSources() {
        HashSet<PNode> sources = new HashSet<PNode>();
        for (PNode node : nodeMap.values()) {
            if (node.isSource()) {
                sources.add(node);
            }
        }
        assert sources.size() > 0;   //otherwise graph would be either empty or cyclic
        return sources;
    }

    void fillLGraphDFS(LGraph lg, PNode root) {
        for (PChild child : root.getChildren()) {
            if (!child.isFake()) {
                for (LNode ln : child.getEdge().getDummyLNodes()) {
                    lg.insertNode(ln);
                }
            }
            if (!lg.isInsertedAlready(child.getNode().getOriginalNode().getLNode())) {
                lg.insertNode(child.getNode().getOriginalNode().getLNode());
                fillLGraphDFS(lg, child.getNode());
            }
        }
    }
}
