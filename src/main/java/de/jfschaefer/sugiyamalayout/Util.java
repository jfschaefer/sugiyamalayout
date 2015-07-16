package de.jfschaefer.sugiyamalayout;

import java.awt.geom.Point2D;

public class Util {
    public static Point2D translatePoint(Point2D a, Point2D b) {
        return new Point2D.Double(a.getX() + b.getX(), a.getY() + b.getY());
    }

    public static Point2D getDelta(Point2D a, Point2D b) {
        return new Point2D.Double(b.getX() - a.getX(), b.getY() - a.getY());
    }

    public static Point2D scalePoint(Point2D a, double l) {
        return new Point2D.Double(a.getX() * l, a.getY() * l);
    }
}
