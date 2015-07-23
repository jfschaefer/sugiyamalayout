package de.jfschaefer.sugiyamalayout;

import java.util.*;

public class PCycle {
    private PNode root;
    private PNode sink;
    // REMARK: BRANCHES GO FROM SINK TO ROOT (for no particular reason) AND DO NOT INCLUDE ROOT AND SINK
    private ArrayList<PNode> leftBranch;
    private ArrayList<PNode> rightBranch;

    private Set<PNode> cycleNodes;

    public static final int NOT_FIXED = 0;
    public static final int BEING_FIXED = 1;
    public static final int FIXED = 2;

    private int state = NOT_FIXED;

    public PCycle(PNode root, PNode sink) {
        this.root = root;
        this.sink = sink;
        leftBranch = new ArrayList<PNode>();
        rightBranch = new ArrayList<PNode>();
        cycleNodes = new HashSet<PNode>();
        cycleNodes.add(root);
        cycleNodes.add(sink);
    }

    public PNode getRoot() {
        return root;
    }


    public PNode getSink() {
        return sink;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void addToLeftBranch(PNode node) {
        leftBranch.add(node);
        cycleNodes.add(node);
        node.addPCycle(this);
    }

    public void addToRightBranch(PNode node) {
        rightBranch.add(node);
        cycleNodes.add(node);
        node.addPCycle(this);
    }

    public Set<PNode> getCycleNodes() {
        return cycleNodes;
    }

    public void swapBranches() {
        ArrayList<PNode> tmp = leftBranch;
        leftBranch = rightBranch;
        rightBranch = tmp;
    }

    public boolean inLeftBranch(PNode node) {
        return leftBranch.contains(node);
    }

    public boolean inRightBranch(PNode node) {
        return rightBranch.contains(node);
    }

    public PNode getTopLeftBranch() {
        return leftBranch.size() == 0 ? sink : leftBranch.get(leftBranch.size() - 1);
    }

    public PNode getTopRightBranch() {
        return rightBranch.size() == 0 ? sink : rightBranch.get(rightBranch.size() - 1);
    }

    public void fix() {
        assert state == NOT_FIXED;
        state = BEING_FIXED;

        System.err.println("\nFIXING " + toString());

        // Check leftBranch/rightBranch are not swapped
        if (root.getChildIndex(getTopLeftBranch()) > root.getChildIndex(getTopRightBranch())) {
            swapBranches();
        }

        // Fix leftBranch/rightBranch
        if (root.childrenCanBeSwapped(getTopLeftBranch(), getTopRightBranch())) {
            System.err.println("   fixing root");
            root.fixChildOrder(getTopLeftBranch(), getTopRightBranch());
        }

        // ((branchNodeIndex, neighbor), descendants)
        Set<Pair<Pair<Integer, PNode>, Set<PNode>>> leftDescendants = new HashSet<Pair<Pair<Integer, PNode>, Set<PNode>>>();
        for (int i = 0; i < leftBranch.size(); i++) {
            PNode branchNode = leftBranch.get(i);
            PNode branchChildNode = i == 0 ? sink : leftBranch.get(i - 1);
            for (PChild neighborChild : branchNode.getChildren()) {
                PNode neighbor = neighborChild.getNode();
                if (neighbor == branchChildNode) continue;
                if (true) {
                //if (branchNode.childrenCanBeSwapped(neighbor, branchChildNode)) {  // otherwise it has been dealt with already
                    Set<PNode> descendants = PNode.getDirectDescendantsInBarrier(neighbor, cycleNodes);
                    descendants.add(neighbor);
                    leftDescendants.add(new Pair<Pair<Integer, PNode>, Set<PNode>>(new Pair<Integer, PNode>(i, neighbor), descendants));
                }
            }
        }

        System.err.println("  leftDescendants: " + leftDescendants);

        // move along right path to check if descendents are common
        for (int i = 0; i < rightBranch.size(); i++) {
            PNode branchNode = rightBranch.get(i);
            PNode branchChildNode = i == 0 ? sink : rightBranch.get(i - 1);
            //for (PChild neighborChild : branchNode.getChildren()) {
            for (int j = 0; j < branchNode.getChildren().size(); j++) {
                PChild neighborChild = branchNode.getChildren().get(j);
                PNode neighbor = neighborChild.getNode();
                if (neighbor == branchChildNode) continue;
                if (branchNode.childrenCanBeSwapped(neighbor, branchChildNode)) {  // otherwise it has been dealt with already
                    Set<PNode> descendants = PNode.getDirectDescendantsInBarrier(neighbor, cycleNodes);
                    descendants.add(neighbor);
                    System.err.println("Right Descendants (" + branchNode + "->" + neighbor + "): " + descendants);
                    boolean foundIntersection = false;
                    // find matching left descendant
                    for (Pair<Pair<Integer, PNode>, Set<PNode>> ld : leftDescendants) {
                        if (Util.setsIntersect(ld.second, descendants)) {
                            foundIntersection = true;
                            // pull neighbors in the cycle and fix them (if they aren't yet)
                            if (branchNode.childrenCanBeSwapped(neighbor, branchChildNode)) {
                                branchNode.moveChildAfter(branchChildNode, neighbor);  //also fixes them
                            }
                            PNode otherBranchNode = leftBranch.get(ld.first.first);
                            PNode otherBranchChildNode = ld.first.first == 0 ? sink : leftBranch.get(ld.first.first - 1);
                            PNode otherNeighbor = ld.first.second;
                            System.err.println("  found intersection: " + branchNode + "->" + neighbor + " - " + otherBranchNode + "->" + otherNeighbor);
                            System.err.println("    " + descendants + "  -  " + ld.second);
                            if (otherBranchNode.childrenCanBeSwapped(otherNeighbor, otherBranchChildNode)) {
                                otherBranchNode.moveChildAfter(otherNeighbor, otherBranchChildNode);
                            }
                            if (otherBranchNode.getChildIndex(otherBranchChildNode) < otherBranchNode.getChildIndex(otherNeighbor) &&
                                    branchNode.getChildIndex(neighbor) < branchNode.getChildIndex(branchChildNode)) {
                                // The descendants are in the cycle :-)
                                PNode.fixLeavesOn(neighbor, sink, cycleNodes);
                                PNode.fixLeavesOn(otherNeighbor, sink, cycleNodes);
                            }
                        }

                    }
                    if (!foundIntersection) {
                        // if they don't have any descendants in common, push neighbors outside
                        boolean isOkay = false;
                        if (branchNode.childrenCanBeSwapped(neighbor, branchChildNode)) {
                            isOkay = true;
                            System.err.println("  swapping " + neighbor + " " + branchChildNode);
                            branchNode.moveChildAfter(neighbor, branchChildNode);   //also fixes them
                        }
                        if (isOkay && branchNode.getChildIndex(neighbor) < branchNode.getChildIndex(branchChildNode)) {
                            System.err.println("  new fixing1 " + neighbor + " on " + sink);
                            PNode.fixLeavesOn(neighbor, sink, cycleNodes);
                        }
                        if (isOkay) {
                            for (PCycle otherCycle: branchNode.getPCycles()) {
                                if (otherCycle == this) continue;
                                if (otherCycle.getState() == NOT_FIXED) {
                                    otherCycle.fix();
                                }
                                if (otherCycle.inLeftBranch(branchNode)) {
                                    PNode otherBranchChildNode = otherCycle.getLeftBranchChild(branchNode);
                                    if (branchNode.getChildIndex(neighbor) > branchNode.getChildIndex(otherBranchChildNode)) {
                                        System.err.println("  fixing " + neighbor + " on " + otherCycle.getSink());
                                        PNode.fixLeavesOn(neighbor, otherCycle.getSink(), otherCycle.getCycleNodes());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    /* if (branchNode.getChildIndex(neighbor) < branchNode.getChildIndex(branchChildNode)) {
                        System.err.println("  WRONG new fixing " + neighbor + " on " + sink);
                        PNode.fixLeavesOn(neighbor, sink, cycleNodes);
                    } */
                }
            }
        }

        // Check the nodes we found on the left branch - if they still aren't fixed, they will be pushed outside the cycle
        for (Pair<Pair<Integer, PNode>, Set<PNode>> ld : leftDescendants) {
            PNode branchNode = leftBranch.get(ld.first.first);
            PNode branchChildNode = ld.first.first == 0 ? sink : leftBranch.get(ld.first.first - 1);
            PNode neighbor = ld.first.second;
            System.err.println("    checking " + branchNode + "  " + neighbor);
            boolean isOkay = false;
            if (branchNode.childrenCanBeSwapped(branchChildNode, neighbor)) {
                isOkay = true;
                System.err.println("  moving " + branchChildNode + " " + neighbor);
                branchNode.moveChildAfter(branchChildNode, neighbor);
            }
            if (isOkay && branchNode.getChildIndex(neighbor) > branchNode.getChildIndex(branchChildNode)) {
                System.err.println("  new fixing2 " + neighbor + " on " + sink);
                //PNode.fixLeavesOn(neighbor, sink, cycleNodes);
            }
            if (isOkay) {
                for (PCycle otherCycle : branchNode.getPCycles()) {
                    if (otherCycle == this) continue;
                    if (otherCycle.getState() == NOT_FIXED) {
                        otherCycle.fix();
                    }
                    if (otherCycle.inRightBranch(branchNode)) {
                        PNode otherBranchChildNode = otherCycle.getRightBranchChild(branchNode);
                        if (branchNode.getChildIndex(neighbor) < branchChildNode.getChildIndex(otherBranchChildNode)) {
                            System.err.println("  fixing " + neighbor + " on " + otherCycle.getSink());
                            PNode.fixLeavesOn(neighbor, otherCycle.getSink(), otherCycle.getCycleNodes());
                        }
                    }
                }
            }
        }


        System.err.println("\nDONE FIXING " + toString());

        state = FIXED;
    }

    public String toString() {

        String s = "PCYCLE Root: " + root.toString() + "   Sink: " + sink.toString();
        String t = "Left: ";
        for (PNode n : leftBranch) t += n.toString() + "  ";
        t += "  Right: ";
        for (PNode n : rightBranch) t += n.toString() + "  ";
        return s + "\n" + t;
    }

    PNode getLeftBranchChild(PNode node) {
        int i = leftBranch.indexOf(node);
        assert i >= 0;
        return i == 0 ? sink : leftBranch.get(i - 1);
    }

    PNode getRightBranchChild(PNode node) {
        int i = rightBranch.indexOf(node);
        assert i >= 0;
        return i == 0 ? sink : rightBranch.get(i - 1);
    }
}
