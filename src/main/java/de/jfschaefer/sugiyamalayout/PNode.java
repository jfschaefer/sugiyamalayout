package de.jfschaefer.sugiyamalayout;

import java.util.*;

class PNode {
    // ordered (!) list of children
    ArrayList<PChild> children = new ArrayList<PChild>();

    Map<PNode, PChild> pnodeTochild = new HashMap<PNode, PChild>();

    // unordered (!) list of parents. The reason I use an ArrayList is that I want to be able to do things like
    // if (parents.size() == 2) { PNode a = parents.get(0); PNode  = parents.get(1) }
    // easily
    ArrayList<PNode> parents = new ArrayList<PNode>();

    // Children who's order had to be changed and is thus fixed.
    // Since this relation is symmetric and transitive, this is an equivalence relation.
    // Represented as a map from nodes to the id of their equivalence class
    Map<PNode, Integer> fixedChildrenOrder = new HashMap<PNode, Integer>();

    //Cycles who contain this node in their branches (that excludes the root and the sink of the cycle)
    Set<PCycle> pcycles = new HashSet<PCycle>();

    Node originalNode;

    boolean root = false;

    PNode(Node original) {
        originalNode = original;
    }

    void addParent(PNode parent) {
        parents.add(parent);
    }

    void addChild(PChild child) {
        children.add(child);
        pnodeTochild.put(child.getNode(), child);
        fixedChildrenOrder.put(child.getNode(), children.size());
    }

    boolean isSource() {
        return parents.isEmpty();
    }

    boolean isSink() {
        return children.isEmpty();
    }

    boolean isRoot() {
        return root;
    }

    void setRoot() {
        root = true;
    }

    void addPCycle(PCycle c) {
        pcycles.add(c);
    }

    Set<PCycle> getPCycles() {
        return pcycles;
    }


    private PNode findLeftmostChild(PNode root, Set<PNode> b) {
        if (b != null) b.add(root);
        return (root.children.isEmpty() ? root : root.children.get(0).getNode());
    }

    private PNode findRightmostChild(PNode root, Set<PNode> b) {
        if (b != null) b.add(root);
        return (root.children.isEmpty() ? root : root.children.get(root.children.size() - 1).getNode());
    }

    // returns true, if there is a path from leaf to the root without crossing the barrier
    private boolean existsDisjointPathToRoot(PNode leaf, Collection<PNode> barrier, Set<PNode> nodes) {
        if (leaf.isRoot()) return true;
        for (PNode parent : leaf.parents) {
            if (!barrier.contains(parent) && existsDisjointPathToRoot(parent, barrier, nodes)) {
                nodes.add(leaf);
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return getOriginalNode() == null ? "[root]" : getOriginalNode().getStringRepresentation();
    }

    void moveChildAfter(PNode first, PNode second) {
        int fi = getChildIndex(first);
        int si = getChildIndex(second);
        PChild child = children.get(fi);
        assert childrenCanBeSwapped(first, second);
        if (fi < si) {
            children.remove(fi);
            // not si + 1, because we've removed an element before, virtually shifting the index by one
            children.add(si, child);
        }
        fixChildOrder(first, second);
    }

    void fixChildOrder(PNode a, PNode b) {
        int newClass = fixedChildrenOrder.get(a);
        int oldClass = fixedChildrenOrder.get(b);
        for (PNode key : fixedChildrenOrder.keySet()) {
            if (fixedChildrenOrder.get(key) == oldClass) {
                fixedChildrenOrder.put(key, newClass);
            }
        }
    }

    static Set<PNode> getDirectDescendants(PNode root) {
        Set<PNode> all = new HashSet<PNode>();
        Set<PNode> nextLayer = new HashSet<PNode>();
        for (PChild child : root.getChildren()) {
            nextLayer.add(child.getNode());
        }
        while (!nextLayer.isEmpty()) {
            Set<PNode> newNext = new HashSet<PNode>();
            for (PNode n : nextLayer) {
                /* if (all.contains(n)) {
                    System.err.println("WARNING: A CYCLE HAS BEEN DETECTED - WE'RE TRYING TO FIX IT");
                    for (int i = 0; i < n.children.size(); i++) {
                        PChild child = n.children.get(i);
                        if (child.isFake()) {
                            n.children.remove(child);
                            child.getNode().parents.remove(n);
                        }
                    }
                    continue;
                } */
                all.add(n);
                for (PChild c : n.children) {
                    if (!c.isFake()) newNext.add(c.getNode());
                }
            }
            nextLayer = newNext;
        }
        return all;
    }
    static Set<PNode> getDirectDescendantsInBarrier(PNode root, Set<PNode> barrier) {
        Set<PNode> all = new HashSet<PNode>();
        Set<PNode> nextLayer = new HashSet<PNode>();
        for (PChild child : root.getChildren()) {
            if (!barrier.contains(child.getNode())) {
                nextLayer.add(child.getNode());
            }
        }
        while (!nextLayer.isEmpty()) {
            Set<PNode> newNext = new HashSet<PNode>();
            for (PNode n : nextLayer) {
                all.add(n);
                for (PChild c : n.getChildren()) {
                    if (!c.isFake() && !barrier.contains(c.getNode())) {
                        newNext.add(c.getNode());
                    }
                }
            }
            nextLayer = newNext;
        }
        return all;
    }

    static void fixLeavesOn(PNode root, PNode fixPoint, Set<PNode> barrier) {
        // adds a fake edge from all the leaves of root to fixpoint unless they go through the barrier
        if (barrier.contains(root)) {
            return;
        }
        if (root.children.isEmpty()) {
            System.err.println("FAKE EDGE: " + root + " -> " + fixPoint);
            root.addChild(new PChild(fixPoint, true, null));
            fixPoint.parents.add(root);
            if (stupidCycleDetector(root, 0)) {
                System.err.println("WARNING: CYCLE DETECTED!!!!WARNING: CYCLE DETECTED!!!!WARNING: CYCLE DETECTED");
            }
        } else {
            for (PChild child : root.getChildren()) {
                if (child.isFake()) continue;   // Is this a good idea??
                fixLeavesOn(child.getNode(), fixPoint, barrier);
            }
        }
    }

    static void fixLeavesCarefullyOn(PNode root, PNode fixPoint, Set<PNode> barrier) {
        // adds a fake edge from all the leaves of root to fixpoint unless they go through the barrier
        // and unless it creates a cycle
        if (barrier.contains(root)) {
            return;
        }
        if (root.children.isEmpty()) {
            System.err.println("PERHAPS FAKE EDGE: " + root + " -> " + fixPoint);
            PChild fakeChild = new PChild(fixPoint, true, null);
            root.addChild(fakeChild);
            fixPoint.parents.add(root);
            if (stupidCycleDetector(root, 0)) {
                System.err.println("UNDOING IT   " + root.children);
                root.children.remove(fakeChild);
                fixPoint.parents.remove(root);
                System.err.println("UNDONE: " + root.children);
            }
        } else {
            for (PChild child : root.getChildren()) {
                if (child.isFake()) continue;   // Is this a good idea??
                fixLeavesCarefullyOn(child.getNode(), fixPoint, barrier);
            }
        }
    }

    static boolean stupidCycleDetector(PNode root, int counter) {
        if (counter > 100) return true;
        for (PChild child : root.getChildren()) {
            return stupidCycleDetector(child.getNode(), counter + 1);
        }
        return false;
    }

    boolean childrenCanBeSwapped(PNode a, PNode b) {
        return fixedChildrenOrder.get(a).intValue() != fixedChildrenOrder.get(b).intValue();
    }

    int getChildIndex(PNode node) {
        return children.indexOf(pnodeTochild.get(node));
    }

    boolean childIsFake(PNode node) {
        System.err.println("CHECKING: " + toString() + "   " + node.toString());
        return pnodeTochild.get(node).isFake();
    }


    Node getOriginalNode() {
        return originalNode;
    }

    Iterator<PNode> getChildPNodeIterator() {
        return new Iterator<PNode>() {
            private int index = 0;
            @Override
            public boolean hasNext() {
                return index < children.size();
            }

            @Override
            public PNode next() {
                return children.get(index++).getNode();
            }
        };
    }

    public Collection<PNode> getParents() {
        return parents;
    }

    public ArrayList<PNode> getParentsAsArrayList() {
        return parents;
    }

    public ArrayList<PChild> getChildren() {
        return children;
    }
}
