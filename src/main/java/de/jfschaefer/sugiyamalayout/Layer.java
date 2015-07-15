package de.jfschaefer.sugiyamalayout;

import java.util.*;

class Layer {
    private ArrayList<LNode> nodes = new ArrayList<LNode>();
    private Configuration config;

    Layer(Configuration configuration) {
        config = configuration;
    }

    void addNode(LNode node) {
        node.setPosition(nodes.size());
        nodes.add(node);
    }

    void parentMedianReordering() {
        final Map<LNode, Integer> medianMap = new HashMap<LNode, Integer>();
        for (LNode lnode : nodes) {
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
                n++;
            }
            for (LNode child : lnode.getChildren()) {
                am += child.getPosition();
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

    void setInitialPixelOffsets() {
        double lastRightPosition = -config.getInitialNodeMargin() * 0.5;
        for (LNode node : nodes) {
            node.setPixelOffset(lastRightPosition + config.getInitialNodeMargin() +
                                node.getPixelOffset() - node.getLeftPixelOffset());
            lastRightPosition = node.getRightPixelOffset();
        }
    }
}
