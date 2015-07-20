package de.jfschaefer.sugiyamalayout;


import java.util.Arrays;

class Edge {
    private Node start, end;
    private boolean reversed = false;
    private LNode[] lnodes = null;

    Edge(Node start, Node end) {
        this.start = start;
        this.end = end;
        start.addOutgoingEdge(this);
        end.addIngoingEdge(this);
    }

    boolean isReversed() {
        return reversed;
    }

    void reverse() {
        start.onEdgeReverse(this);
        end.onEdgeReverse(this);
        reversed = !reversed;
    }

    Node getStart() {
        return reversed ? end : start;
    }

    Node getEnd() {
        return reversed ? start : end;
    }

    void reset() {
        reversed = false;
    }

    void generateLNodes() {
        Node start = getStart();
        Node end = getEnd();
        int size = end.getLayer() - start.getLayer() + 1;
        lnodes = new LNode[size];
        lnodes[0] = start.getLNode();
        lnodes[size - 1] = end.getLNode();
        for (int i = 1; i < size - 1; i++) {
            lnodes[i] = new LNode(this, i + start.getLayer());
        }
        for (int i = 1; i < size; i++) {
            lnodes[i].addParent(lnodes[i-1]);
            lnodes[i-1].addChild(lnodes[i]);
        }
    }

    LNode[] getLNodes() {
        if (lnodes == null) generateLNodes();
        return lnodes;
    }

    LNode[] getDummyLNodes() {
        if (lnodes == null) generateLNodes();
        return Arrays.copyOfRange(lnodes, 1, lnodes.length - 1);
    }
}
