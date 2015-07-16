package de.jfschaefer.sugiyamalayout;

class PChild {
    boolean fake;
    Edge edge;
    PNode child;
    PChild(PNode child, boolean fake, Edge edge) {
        assert fake == (edge == null);
        this.fake = fake;
        this.edge = edge;
        this.child = child;
    }

    boolean isFake() {
        return fake;
    }

    Edge getEdge() {
        return edge;
    }

    PNode getNode() {
        return child;
    }
}
