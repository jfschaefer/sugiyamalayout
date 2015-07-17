package de.jfschaefer.sugiyamalayout;

import java.lang.reflect.Array;
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

    Node originalNode;

    boolean root = false;

    boolean resolved = false;


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

    boolean isResolved() {
        return resolved;
    }

    boolean isRoot() {
        return root;
    }

    void setRoot() {
        root = true;
    }

    boolean tryToResolve() {
        if (isResolved()) return false;

        // try to resolve children
        boolean achievedSomething = false;
        for (int i = 0; i < children.size(); i++) {
            PChild child = children.get(i);
            if (child.getNode() == this) continue;
            achievedSomething |= child.getNode().tryToResolve();
        }
        if (achievedSomething) {
            // this weird mechanism is due to the fact that the order of the children might have been changed,
            // which has caused some children to be skipped in the past
            while (achievedSomething) {
                achievedSomething = false;
                for (int i = 0; i < children.size(); i++) {
                    PChild child = children.get(i);
                    if (child.getNode() == this || child.isFake()) continue;
                    achievedSomething |= child.getNode().tryToResolve();
                }
            }
            achievedSomething = true;
        }

        Set<PNode> unresolvedChildren = new HashSet<PNode>();
        for (PChild child : children) {
            if (!child.isFake() && !child.getNode().isResolved()) {
                unresolvedChildren.add(child.getNode());
            }
        }
        boolean allChildrenAreResolved = unresolvedChildren.isEmpty();

        // RULE 1: All children are resolved and at most 1 parent => current node is resolved
        if (allChildrenAreResolved && parents.size() <= 1) {
            resolved = true;
            return true;
        }

        // RULE 2: All children are resolved and node has (at least) two non-resolved parents
        if (allChildrenAreResolved && parents.size() >= 2) {
            PNode a = null;
            PNode b = null;
            for (PNode parent : parents) {
                if (a == null && !parent.isResolved() && !parent.childIsFake(this)) a = parent;
                else if (b == null && !parent.isResolved() && !parent.childIsFake(this)) b = parent;
            }
            if (b != null) {  // found to such parents
                // find a path from a to the root
                ArrayList<PNode> pathA = new ArrayList<PNode>();
                pathA.add(a);
                while (!a.isRoot()) {
                    a = a.parents.get(0);
                    pathA.add(a);
                }
                // find a path from b to the path we just found
                ArrayList<PNode> pathB = new ArrayList<PNode>();
                while (!pathA.contains(b)) {
                    pathB.add(b);
                    b = b.parents.get(0);
                }
                PNode commonRoot = b;
                int cutOffIndex = pathA.indexOf(commonRoot);
                while (cutOffIndex < pathA.size()) {
                    pathA.remove(pathA.size() - 1);
                }

                ArrayList<PNode> leftPath = null;
                ArrayList<PNode> rightPath = null;
                for (PChild child : commonRoot.getChildren()) {
                    if (!pathA.isEmpty() && child.getNode() == pathA.get(pathA.size() - 1)) {
                        leftPath = pathA;
                        rightPath = pathB;
                        break;
                    } else if (!pathB.isEmpty() && child.getNode() == pathB.get(pathB.size() - 1)) {
                        leftPath = pathB;
                        rightPath = pathA;
                        break;
                    }
                    if (child.getNode() == this) {
                        if (pathA.isEmpty()) {
                            leftPath = pathA;
                            rightPath = pathB;
                            break;
                        } else if (pathB.isEmpty()) {
                            leftPath = pathB;
                            rightPath = pathA;
                            break;
                        }
                    }
                }

                Set<PNode> cycleNodes = new HashSet<PNode>();
                for (PNode n : rightPath) cycleNodes.add(n);
                for (PNode n : leftPath) cycleNodes.add(n);
                cycleNodes.add(this);
                cycleNodes.add(commonRoot);

                // left path descendants (path node, (pathchild, neighbor), descendants)
                Set<Triple<PNode, Pair<PNode, PNode>, Set<PNode>>> lpdesc = new HashSet<Triple<PNode, Pair<PNode, PNode>, Set<PNode>>>();

                for (int pathni = 0; pathni < leftPath.size(); pathni++) {
                    PNode pathnode = leftPath.get(pathni);

                    int pathChildIndex = pathnode.getChildIndex(pathni == 0 ? this : leftPath.get(pathni - 1));
                    PNode pathChild = pathnode.children.get(pathChildIndex).getNode();
                    for (int i = 0; i < pathnode.getChildren().size(); i++) {
                        PNode neighbor = pathnode.children.get(i).getNode();

                        if (i < pathChildIndex && pathnode.childrenCanBeSwapped(neighbor, pathChild)) {
                            // if they couldn't be swapped, we couldn't do anything
                            lpdesc.add(new Triple<PNode, Pair<PNode, PNode>, Set<PNode>>
                                    (pathnode, new Pair<PNode, PNode>(pathChild, neighbor), getDirectDescendants(neighbor)));
                        }
                        if (i > pathChildIndex) {
                            if (pathnode.childrenCanBeSwapped(neighbor, pathChild)) {
                                lpdesc.add(new Triple<PNode, Pair<PNode, PNode>, Set<PNode>>
                                        (pathnode, new Pair<PNode, PNode>(pathChild, neighbor), getDirectDescendants(neighbor)));
                            } else {
                                System.err.println("FIX LEAVES 00");
                                pathnode.fixChildOrder(neighbor, pathChild);
                                fixLeavesOn(neighbor, this, cycleNodes);
                            }
                        }
                    }
                }


                // now iterate along right path
                for (int pathni = 1; pathni < rightPath.size(); pathni++) {
                    PNode pathnode = rightPath.get(pathni);
                    int pathChildIndex = pathnode.getChildIndex(rightPath.get(pathni - 1));
                    PNode pathChild = pathnode.children.get(pathChildIndex).getNode();


                    for (Triple<PNode, Pair<PNode, PNode>, Set<PNode>> triple : lpdesc) {
                        if (triple.third.contains(pathnode)) {
                            if (triple.first.getChildIndex(triple.second.first) > triple.first.getChildIndex(triple.second.second)) {
                                // we have to swap the children
                                System.err.println("MOVE IN 00");
                                achievedSomething = true;
                                triple.first.moveChildAfter(triple.second.second, triple.second.first);
                                fixLeavesOn(triple.second.first, this, cycleNodes);
                            }
                        }
                    }

                    for (int i = 0; i < pathnode.getChildren().size(); i++) {
                        PNode neighbor = pathnode.children.get(i).getNode();
                        Set<PNode> myDescendents = getDirectDescendants(neighbor);
                        if (i < pathChildIndex || (i > pathChildIndex && pathnode.childrenCanBeSwapped(neighbor, pathChild))) {
                            for (Triple<PNode, Pair<PNode, PNode>, Set<PNode>> triple : lpdesc) {
                                if (triple.third.contains(pathnode)) {
                                    if (triple.first.getChildIndex(triple.second.first) > triple.first.getChildIndex(triple.second.second)) {
                                        // we have to swap the children
                                        achievedSomething = true;
                                        triple.first.moveChildAfter(triple.second.second, triple.second.first);
                                        if (i > pathChildIndex) {
                                            moveChildAfter(pathChild, neighbor);
                                        }
                                        fixLeavesOn(triple.second.first, this, cycleNodes);
                                    }
                                }
                            }
                        }
                    }
                }

                //System.err.println("CYCLE: " + cycleNodes);
                System.err.print("CYCLE: ");
                for (PNode n : cycleNodes) {
                    System.err.print(n.myToString() + "      ");
                }
                System.err.println("");
                // Push everything out that hasn't been pulled in
                for (int pathni = 0; pathni < leftPath.size(); pathni++) {
                    PNode pathnode = leftPath.get(pathni);

                    int pathChildIndex = pathnode.getChildIndex(pathni == 0 ? this : leftPath.get(pathni - 1));
                    PNode pathChild = pathnode.children.get(pathChildIndex).getNode();
                    for (int i = 0; i < pathnode.getChildren().size(); i++) {
                        PNode neighbor = pathnode.children.get(i).getNode();


                        if (i > pathChildIndex && pathnode.childrenCanBeSwapped(neighbor, pathChild)) {
                            // have to fix leaves, if we push it into a new cycle
                            Set<PNode> fixingBarrier = new HashSet<PNode>();
                            PNode lmc = pathnode.findLeftmostChild(pathnode, fixingBarrier);

                            System.err.println("MOVE OUT 00");
                            achievedSomething = true;
                            pathnode.moveChildAfter(pathChild, neighbor);
                            System.err.println(pathnode.myToString() + "   PC: " + pathChild.myToString() + "   NB: " + neighbor.myToString() + "   NC: " + pathnode.children.size());

                            Set<PNode> barrier = new HashSet<PNode>();
                            barrier.add(pathnode);
                            for (PNode n : rightPath) {
                                barrier.add(n);
                            }
                            if (!pathnode.isRoot() && !pathnode.parents.get(0).isRoot() && existsDisjointPathToRoot(lmc, barrier, fixingBarrier)) {
                                System.err.println("FIXING " + neighbor.myToString() + " on " + lmc.myToString());
                                fixLeavesOn(neighbor, lmc, fixingBarrier);
                            }
                        } else {
                            pathnode.fixChildOrder(neighbor, pathChild);
                        }
                    }
                }

                for (int pathni = 0; pathni < rightPath.size(); pathni++) {
                    PNode pathnode = rightPath.get(pathni);

                    int pathChildIndex = pathnode.getChildIndex(pathni == 0 ? this : rightPath.get(pathni - 1));
                    PNode pathChild = pathnode.children.get(pathChildIndex).getNode();
                    for (int i = 0; i < pathnode.getChildren().size(); i++) {
                        PNode neighbor = pathnode.children.get(i).getNode();

                        if (i < pathChildIndex && pathnode.childrenCanBeSwapped(neighbor, pathChild)) {

                            // have to fix leaves, if we push it into a new cycle
                            Set<PNode> fixingBarrier = new HashSet<PNode>();
                            PNode rmc = pathnode.findRightmostChild(pathnode, fixingBarrier);

                            System.err.println("MOVE OUT 01");
                            System.err.println(pathnode.myToString() + "   PC: " + pathChild.myToString() + "   NB: " + neighbor.myToString() + "  NC: " + pathnode.children.size());
                            System.err.println(children);
                            achievedSomething = true;

                            pathnode.moveChildAfter(neighbor, pathChild);
                            Set<PNode> barrier = new HashSet<PNode>();
                            barrier.add(pathnode);
                            for (PNode n : rightPath) {
                                barrier.add(n);
                            }
                            if (!pathnode.isRoot() && !pathnode.parents.get(0).isRoot() && existsDisjointPathToRoot(rmc, barrier, fixingBarrier)) {
                                System.err.println("FIXING " + neighbor.myToString() + " on " + rmc.myToString());
                                fixLeavesOn(neighbor, rmc, fixingBarrier);
                            }
                        } else {
                            pathnode.fixChildOrder(neighbor, pathChild);
                        }
                    }
                }
                for (PNode pathnode : cycleNodes) {
                    if (pathnode.allDependenciesResolvedOrInSet(cycleNodes)) {
                        if (!pathnode.isResolved()) {
                            achievedSomething = true;
                        }
                        pathnode.resolved = true;
                    }
                }
            }
        }
        return achievedSomething;
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

    String myToString() {
        return getOriginalNode() == null ? "null" : getOriginalNode().getStringRepresentation();
    }

    boolean allDependenciesResolvedOrInSet(Set<PNode> set) {
        for (PChild child : children) {
            if (!child.getNode().isResolved() && !set.contains(child.getNode())) return false;
        }
        for (PNode parent : parents) {
            if (!parent.isResolved() && !set.contains(parent)) return false;
        }
        return true;
    }

    void moveChildAfter(PNode first, PNode second) {
        System.err.print("MOVE - before: ");
        for (PChild child : children) {
            System.err.print(child.getNode().myToString() + "   ");
        }
        int fi = getChildIndex(first);
        int si = getChildIndex(second);
        PChild child = children.get(fi);
        assert fi < si;
        assert childrenCanBeSwapped(first, second);
        children.remove(fi);
        // not si + 1, because we've removed an element before, virtually shifting the index by one
        children.add(si, child);
        System.err.print("\nMOVE - after: ");
        for (PChild childd : children) {
            System.err.print(childd.getNode().myToString() + "   ");
        }
        System.err.println("");
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

    Set<PNode> getDirectDescendants(PNode root) {
        Set<PNode> all = new HashSet<PNode>();
        Set<PNode> nextLayer = new HashSet<PNode>();
        for (PChild child : root.getChildren()) {
            nextLayer.add(child.getNode());
        }
        while (!nextLayer.isEmpty()) {
            Set<PNode> newNext = new HashSet<PNode>();
            for (PNode n : nextLayer) {
                if (all.contains(n)) {
                    System.err.println("WARNING: A CYCLE HAS BEEN DETECTED - WE'RE TRYING TO FIX IT");
                    for (int i = 0; i < n.children.size(); i++) {
                        PChild child = n.children.get(i);
                        if (child.isFake()) {
                            n.children.remove(child);
                            child.getNode().parents.remove(n);
                        }
                    }
                    continue;
                }
                all.add(n);
                for (PChild c : n.children) {
                    newNext.add(c.getNode());
                }
            }
            nextLayer = newNext;
        }
        return all;
    }

    void fixLeavesOn(PNode root, PNode fixPoint, Set<PNode> barrier) {
        // adds a fake edge from all the leaves of root to fixpoint unless they go through the barrier
        System.err.println("  ON " + fixPoint.myToString());
        if (barrier.contains(root)) {
            System.err.println("  blocked by " + root.myToString());
            return;
        }
        if (root.children.isEmpty()) {
            System.err.println("  fixing " + root.myToString());
            root.addChild(new PChild(fixPoint, true, null));
            fixPoint.parents.add(root);
        } else {
            for (PChild child : root.getChildren()) {
                fixLeavesOn(child.getNode(), fixPoint, barrier);
            }
        }
    }

   /* void fixLeavesOn(PNode root, PNode fixPoint) {
        // adds a fake edge from all the leaves of root to fixpoint
        if (root.children.isEmpty()) {
            root.children.add(new PChild(fixPoint, true, null));
        } else {
            for (PChild child : root.getChildren()) {
                System.err.println(child.getNode() + "   " + child.getEdge());
                fixLeavesOn(child.getNode(), fixPoint);
            }
        }
    } */

    boolean childrenCanBeSwapped(PNode a, PNode b) {
        return fixedChildrenOrder.get(a).intValue() != fixedChildrenOrder.get(b).intValue();
    }

    int getChildIndex(PNode node) {
        return children.indexOf(pnodeTochild.get(node));
    }

    boolean childIsFake(PNode node) {
        System.err.println("CHECKING: " + myToString() + "   " + node.myToString());
        return pnodeTochild.get(node).isFake();
    }

    boolean allChildrenResolved() {
        for (PChild child : children) {
            if (!child.getNode().isResolved()) return false;
        }
        return true;
    }

    boolean allChildrenExcResolved(PNode exceptFor) {
        for (PChild child : children) {
            if (child.getNode() == exceptFor) continue;
            if (!child.getNode().isResolved()) return false;
        }
        return true;
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

    public ArrayList<PChild> getChildren() {
        return children;
    }
}
