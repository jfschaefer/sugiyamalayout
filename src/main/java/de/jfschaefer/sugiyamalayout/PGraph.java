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

        runAlgorithm();

        setLayers();
        LGraph lg = new LGraph(config);
        fillLGraphDFS(lg, root);
        return lg;
    }


    public void runAlgorithm() {
        Random random = new Random();
        // parents with at least two children
        Set<PNode> interestingParents = new HashSet<PNode>();
        // children with at least two parents
        Set<PNode> interestingChildren = new HashSet<PNode>();

        for (PNode node : nodeMap.values()) {
            if (node.getChildren().size() >= 2) {
                interestingParents.add(node);
            }
            if (node.getParents().size() >= 2) {
                interestingChildren.add(node);
            }
        }

        /*
            FIND INITIAL CYCLES
         */

        System.err.println("INITIAL CYCLES\n==============\n");

        ArrayList<PCycle> initialPCycles = new ArrayList<PCycle>();
        for (PNode sink : interestingChildren) {
            ArrayList<PNode> parents = sink.getParentsAsArrayList();
            for (int i = 0; i < parents.size(); i++) {
                for (int j = i + 1; j < parents.size(); j++) {
                    PNode a = parents.get(i);
                    PNode b = parents.get(j);

                    ArrayList<PNode> pathA = new ArrayList<PNode>();
                    pathA.add(a);
                    while (!a.isRoot()) {
                        a = a.getParentsAsArrayList().get(random.nextInt(a.getParents().size()));
                        pathA.add(a);
                    }

                    ArrayList<PNode> pathB = new ArrayList<PNode>();
                    while (!pathA.contains(b)) {
                        pathB.add(b);
                        b = b.getParentsAsArrayList().get(random.nextInt(b.getParents().size()));
                    }

                    PCycle cycle = new PCycle(b, sink);
                    for (PNode p : pathA) {
                        if (p == b) break;
                        cycle.addToLeftBranch(p);
                        a = p;
                    }
                    for (PNode p : pathB) {
                        cycle.addToRightBranch(p);
                    }

                    if (b.getChildIndex(a) > b.getChildIndex(pathB.size() == 0 ? sink : pathB.get(pathB.size() - 1))) {
                        cycle.swapBranches();
                    }
                    initialPCycles.add(cycle);
                    System.err.println(cycle.toString());
                }
            }

        }

        // The depths of the nodes. Node that these will change during the process as so called fake edges can be added.
        // Due to the nature of the algorithm, it's not important to update the depths though.
        // Of course new depths have to be calculate in the end for the LGraph generation
        final Map<PNode, Integer> pnodeDepths = new HashMap<PNode, Integer>();
        pnodeDepths.put(root, 1);
        setPNDepths(pnodeDepths, root, 1);

        // Sort initialPCycles by increasing depth of sinks (not sure if it helps, but intuitively, it's a good idea
        Collections.sort(initialPCycles, new Comparator<PCycle>() {
            @Override
            public int compare(PCycle o1, PCycle o2) {
                return pnodeDepths.get(o2.getSink()).compareTo(pnodeDepths.get(o1.getSink()));
            }
        });

        for (PCycle cycle : initialPCycles) {
            if (cycle.getState() == PCycle.FIXED) continue;
            cycle.fix();
        }
    }

    private void setPNDepths(Map<PNode, Integer> depths, PNode root, int rootDepth) {
        for (PChild child : root.getChildren()) {
            PNode n = child.getNode();
            if (!depths.containsKey(n) || depths.get(n) < rootDepth + 1) {
                depths.put(n, rootDepth + 1);
                setPNDepths(depths, n, rootDepth + 1);
            }
        }
    }






    /*
        AFTER ALGORITHM: SET LAYERS AND POPULATE (FILL) LGRAPH
     */

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
