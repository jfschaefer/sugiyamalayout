package de.jfschaefer.sugiyamalayout;

public class Pair<A, B> {
    public final A first;
    public final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public String toString() {
        return "(" + this.first.toString() + ", " + this.second.toString() + ")";
    }
}
