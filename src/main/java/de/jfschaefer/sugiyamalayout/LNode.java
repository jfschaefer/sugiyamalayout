package de.jfschaefer.sugiyamalayout;

import java.util.*;

class LNode {
    private Node node = null;   //stays null if dummy node
    private Edge edge = null;   //stays null if normal (non-dummy) node
    private Set<LNode> parents = new HashSet<LNode>();
    private Set<LNode> children = new HashSet<LNode>();
    private int layer;
    private int position;
    private Configuration config = null;
    private double pixelOffset = 0d;


    LNode(Node node, int layer) {
        this.node = node;
        this.layer = layer;
    }

    LNode(Edge edge, int layer) {
        this.edge = edge;
        this.layer = layer;
    }

    void addChild(LNode n) {
        children.add(n);
    }

    void addParent(LNode n) {
        parents.add(n);
    }

    int getLayer() {
        return layer;
    }

    boolean isDummy() {
        return node == null;
    }

    void setConfiguration(Configuration configuration) {
        config = configuration;
    }

    int getPosition() {
        return position;
    }

    void setPosition(int position) {
        this.position = position;
    }

    Collection<LNode> getParents() {
        return parents;
    }

    Collection<LNode> getChildren() {
        return children;
    }

    double getWidth() {
        if (node == null) {
            return config.getDummyNodeWidth();
        } else {
            return node.getWidth();
        }
    }

    double getHeight() {
        if (node == null) {
            return 0d;
        } else {
            return node.getHeight();
        }
    }

    double getPixelOffset() {
        return pixelOffset;
    }

    double getLeftPixelOffset() {
        return pixelOffset - 0.5 * getWidth();
    }

    double getRightPixelOffset() {
        return pixelOffset + 0.5 * getWidth();
    }

    void setPixelOffset(double pixelOffset) {
        this.pixelOffset = pixelOffset;
    }

    double getOutEdgeXPos(LNode towards) {
        if (config.isEdgeCentralization()) {
            return getPixelOffset();
        } else {
            assert children.contains(towards);
            int numberOfChildrenToTheLeft = 0;
            for (LNode child : children) {
                if (child.getPosition() < towards.getPosition()) {
                    numberOfChildrenToTheLeft ++;
                }
            }
            double relativePosition = ((double)numberOfChildrenToTheLeft + 1d) / ((double)children.size() + 1d);
            return getLeftPixelOffset() + relativePosition * getWidth();
        }
    }

    double getInEdgeXPos(LNode from) {
        if (config.isEdgeCentralization()) {
            return getPixelOffset();
        } else {
            assert parents.contains(from);
            int numberOfParentsToTheLeft = 0;
            for (LNode parent : parents) {
                if (parent.getPosition() < from.getPosition()) {
                    numberOfParentsToTheLeft ++;
                }
            }
            double relativePosition = ((double)numberOfParentsToTheLeft + 1d) / ((double)parents.size() + 1d);
            return getLeftPixelOffset() + relativePosition * getWidth();
        }
    }
}
