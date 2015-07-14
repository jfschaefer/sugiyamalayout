package de.jfschaefer.sugiyamalayout;

import java.awt.geom.Point2D;

public class Util {
    public static Point2D translatePoint(Point2D a, Point2D b) {
        return new Point2D.Double(a.getX() + b.getX(), a.getY() + b.getY());
    }
}
