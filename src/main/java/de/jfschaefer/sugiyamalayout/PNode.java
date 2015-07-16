package de.jfschaefer.sugiyamalayout;

import java.util.*;

class PNode {
    // ordered (!) list of children
    ArrayList<PChild> children = new ArrayList<PChild>();

    Set<PNode> parents = new HashSet<PNode>();

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

    void tryToResolve() {
        for (PChild child : children) {
            if (!child.getNode().isResolved()) {
                child.getNode().tryToResolve();
            }
        }

        boolean allChildrenAreResolved = true;
        for (PChild child : children) {
            if (!child.getNode().isResolved()) {
                allChildrenAreResolved = false;
                break;
            }
        }

        if (allChildrenAreResolved && parents.size() <= 1) {
            resolved = true;
        }
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

    public Set<PNode> getParents() {
        return parents;
    }

    public ArrayList<PChild> getChildren() {
        return children;
    }
}
