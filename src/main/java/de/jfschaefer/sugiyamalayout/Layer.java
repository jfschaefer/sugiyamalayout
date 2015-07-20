package de.jfschaefer.sugiyamalayout;

import java.util.*;

class Layer {
    private ArrayList<LNode> nodes = new ArrayList<LNode>();
    private Configuration config;
    private Layer parentLayer = null;
    private Layer childrenLayer = null;

    Layer(Configuration configuration) {
        config = configuration;
    }

    void addNode(LNode node) {
        node.setPosition(nodes.size());
        nodes.add(node);
    }

    void setParentLayer(Layer parentLayer) {
        this.parentLayer = parentLayer;
    }

    void setChildrenLayer(Layer childrenLayer) {
        this.childrenLayer = childrenLayer;
    }

    ArrayList<LNode> getNodes() {
        return nodes;
    }

    void parentMedianReordering() {
        final Map<LNode, Integer> medianMap = new HashMap<LNode, Integer>();
        for (LNode lnode : nodes) {
            if (lnode.getParents().isEmpty()) {
                medianMap.put(lnode, -1);
                continue;
            }
            TreeSet<Integer> positions = new TreeSet<Integer>();
            for (LNode parent : lnode.getParents()) {
                positions.add(parent.getPosition());
            }
            int i = 0;
            Iterator<Integer> iter = positions.iterator();

            while (i++ < positions.size() / 2) {
                iter.next();
            }
            medianMap.put(lnode, iter.next());
        }
        Collections.sort(nodes, new Comparator<LNode>() {
            @Override
            public int compare(LNode a, LNode b) {
                return medianMap.get(a).compareTo(medianMap.get(b));
            }
        });
        //update positions
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).setPosition(i);
        }
    }

    void medianReordering() {
        final Map<LNode, Integer> medianMap = new HashMap<LNode, Integer>();
        for (LNode lnode : nodes) {
            TreeSet<Integer> positions = new TreeSet<Integer>();
            for (LNode parent : lnode.getParents()) {
                positions.add(parent.getPosition());
            }
            for (LNode child : lnode.getChildren()) {
                positions.add(child.getPosition());
            }
            if (positions.size() == 0) {
                medianMap.put(lnode, -1);
            }
            int i = 0;
            Iterator<Integer> iter = positions.iterator();
            while (i++ < positions.size() / 2) {
                iter.next();
            }
            medianMap.put(lnode, iter.next());
        }
        Collections.sort(nodes, new Comparator<LNode>() {
            @Override
            public int compare(LNode a, LNode b) {
                return medianMap.get(a).compareTo(medianMap.get(b));
            }
        });
        //update positions
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).setPosition(i);
        }
    }

    void arithmeticMeanReordering() {
        final Map<LNode, Double> amMap = new HashMap<LNode, Double>();
        for (LNode lnode : nodes) {
            double am = 0d;
            int n = 0;
            for (LNode parent : lnode.getParents()) {
                am += parent.getPosition();
               // am += (parent.getPosition() + 0.5) * nodes.size(); //(lnode.getChildren().size() == 0 ? 1 : lnode.getChildren().size());
                n++;
            }
            for (LNode child : lnode.getChildren()) {
                am += child.getPosition();
                // am += (child.getPosition() + 0.5); //(lnode.getParents().size() == 0 ? 1 : lnode.getParents().size());
                n++;
            }
            amMap.put(lnode, n == 0 ? -1 : am / n);
        }
        Collections.sort(nodes, new Comparator<LNode>() {
            @Override
            public int compare(LNode a, LNode b) {
                //return amMap.get(a).compareTo(amMap.get(b));
                double a2 = amMap.get(a);
                double b2 = amMap.get(b);
                if (a2 < b2) return -1;
                if (a2 == b2) return 0;
                if (a2 > b2) return 1;
                return 0;
            }
        });
        //update positions
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).setPosition(i);
        }
    }

    int getNumberOfCrossings(int nodePos) {
        LNode node = nodes.get(nodePos);
        int layer = node.getLayer();
        int crossings = 0;
        for (LNode parent : node.getParents()) {
            for (LNode sibling : nodes) {
                if (sibling == node) continue;
                for (LNode uncle : sibling.getParents()) {
                    if (sibling.getPosition() < node.getPosition() && uncle.getPosition() > parent.getPosition()) {
                        crossings++;
                    } else
                    if (sibling.getPosition() > node.getPosition() && uncle.getPosition() < parent.getPosition()) {
                        crossings++;
                    }
                }
            }
        }
        for (LNode child : node.getChildren()) {
            for (LNode sibling : nodes) {
                if (sibling == node) continue;
                for (LNode nephew : sibling.getChildren()) {
                    if (sibling.getPosition() < node.getPosition() && nephew.getPosition() > child.getPosition()) {
                        crossings++;
                    } else
                    if (sibling.getPosition() > node.getPosition() && nephew.getPosition() < child.getPosition()) {
                        crossings++;
                    }
                }
            }
        }
        return crossings;
    }

    boolean greedyOptimization() {
        //swap neighboring nodes if it reduces the number of crossings
        boolean swappedSomething = false;
        int iterationCounter = 0;
        do {
            swappedSomething = false;
            for (int i = 0; i < nodes.size() - 1; i++) {
                int currentTotalCrossings = getNumberOfCrossings(i) + getNumberOfCrossings(i + 1);
                // swap nodes
                LNode tmp = nodes.get(i);
                nodes.set(i, nodes.get(i+1));
                nodes.set(i + 1, tmp);
                nodes.get(i + 1).setPosition(i + 1);
                nodes.get(i).setPosition(i);
                int newTotalCrossings = getNumberOfCrossings(i) + getNumberOfCrossings(i + 1);
                if (newTotalCrossings < currentTotalCrossings) {
                    swappedSomething = true;
                } else {
                    // swap nodes back
                    tmp = nodes.get(i);
                    nodes.set(i, nodes.get(i+1));
                    nodes.set(i + 1, tmp);
                    nodes.get(i + 1).setPosition(i + 1);
                    nodes.get(i).setPosition(i);
                }
            }
            iterationCounter++;
        } while (swappedSomething);

        return (iterationCounter > 1);
    }

    void setInitialPixelOffsets() {
        double lastRightPosition = -config.getInitialNodeMargin() * 0.5;
        for (LNode node : nodes) {
            node.setPixelOffset(lastRightPosition + config.getInitialNodeMargin() +
                                node.getPixelOffset() - node.getLeftPixelOffset());
            lastRightPosition = node.getRightPixelOffset();
        }
    }

    void relaxPixelOffsets() {
        for (int i = 0; i < nodes.size(); i++) {
            LNode node = nodes.get(i);
            double perfectPosition = 0d;
            for (LNode parent : node.getParents()) {
                perfectPosition += parent.getOutEdgeXPos(node);
            }
            for (LNode child : node.getChildren()) {
                perfectPosition += child.getInEdgeXPos(node);
            }
            perfectPosition /= node.getParents().size() + node.getChildren().size() + 0.000000001;   // No zero division
            if (i + 1 < nodes.size()) {
                perfectPosition = Math.min(perfectPosition, nodes.get(i+1).getLeftPixelOffset() -
                        0.5 * node.getWidth() - config.getMinimalNodeMargin());
            }
            if (i > 0) {
                perfectPosition = Math.max(perfectPosition, nodes.get(i-1).getRightPixelOffset() +
                        0.5 * node.getWidth()+ config.getMinimalNodeMargin());
            }
            node.setPixelOffset(perfectPosition);

            // if node has only sinks as children, distribute them "evenly"
            if (node.getChildren().size() > 1) {
                boolean onlySinks = true;
                ArrayList<Integer> positions = new ArrayList<Integer>();
                for (LNode child : node.getChildren()) {
                    positions.add(child.getPosition());
                    if (child.getChildren().size() > 0) {
                        onlySinks = false;
                        break;
                    }
                }
                if (onlySinks) {
                    Collections.sort(positions);
                    if (positions.size() - 1 == positions.get(positions.size() - 1) - positions.get(0)) {
                        // children have to be consecutive
                        double leftmost = childrenLayer.nodes.get(positions.get(0)).getLeftPixelOffset();
                        double rightmost = childrenLayer.nodes.get(positions.get(positions.size()-1)).getRightPixelOffset();
                        double shift = node.getPixelOffset() - 0.5 * (childrenLayer.nodes.get(positions.get(0)).getPixelOffset() +
                                                childrenLayer.nodes.get(positions.get(positions.size() - 1)).getPixelOffset());
                        double minleft = leftmost + shift;
                        if (positions.get(0) != 0) {
                            minleft = Math.max(childrenLayer.nodes.get(positions.get(0)-1).getRightPixelOffset() + config.getMinimalNodeMargin(), minleft);
                        }
                        double maxRight = rightmost + shift;
                        if (positions.get(positions.size()-1) != childrenLayer.nodes.size() - 1) {
                            maxRight = Math.min(childrenLayer.nodes.get(positions.get(positions.size() - 1) + 1).getLeftPixelOffset() - config.getMinimalNodeMargin(), maxRight);
                        }
                        if (leftmost + shift < minleft) {
                            shift = minleft - leftmost;
                        }
                        if (rightmost + shift > maxRight) {
                            shift = maxRight - rightmost;
                        }

                        for (LNode child : node.getChildren()) {
                            child.setPixelOffset(child.getPixelOffset() + shift);
                        }
                    }
                }
            }

            /*
            // try moving entire subtree closer to the parent's offset
            perfectPosition = 0d;
            for (LNode parent : node.getParents()) {
                perfectPosition += parent.getOutEdgeXPos(node);
            }
            perfectPosition /= node.getParents().size();
            double shift = node.getPixelOffset() - perfectPosition;
            if (shift < 0) {

            }
            */
        }
    }
}
