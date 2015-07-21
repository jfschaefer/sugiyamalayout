package de.jfschaefer.sugiyamalayout;

import java.util.*;
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

    public static boolean setsIntersect(Set a, Set b) {
        Set smaller = a;
        Set larger = b;
        if (smaller.size() > larger.size()) {
            smaller = b;
            larger = a;
        }
        for (Object x : smaller) {
            if (larger.contains(x)) return true;
        }
        return true;
    }

    public static Point2D bezier(Point2D start, Point2D control1, Point2D control2, Point2D end, double lambda) {
        return translatePoint(scalePoint(start, (1-lambda)*(1-lambda)*(1-lambda)),
                translatePoint(scalePoint(control1, 3*(1-lambda)*(1-lambda)*lambda),
                translatePoint(scalePoint(control2, 3*(1-lambda)*lambda*lambda),
                scalePoint(end, lambda*lambda*lambda))));
    }

    public static Point2D bezierDerivative(Point2D start, Point2D control1, Point2D control2, Point2D end, double lambda) {
        return translatePoint(scalePoint(getDelta(start, control1), 3*(1-lambda)*(1-lambda)),
                translatePoint(scalePoint(getDelta(control1, control2), 6*(1-lambda)*lambda),
                scalePoint(getDelta(control2, end), 3*lambda*lambda)));
    }
}
