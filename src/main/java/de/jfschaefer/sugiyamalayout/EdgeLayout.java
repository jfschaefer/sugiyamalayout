package de.jfschaefer.sugiyamalayout;

import java.awt.geom.Point2D;
import java.util.*;

// While the node layout is pretty much determined by the center point,
// the edge layout is more complicated, so it gets its own class
public class EdgeLayout {
    private Configuration config;
    private ArrayList<Point2D> points = new ArrayList<Point2D>();
    //position, angle
    private Pair<Point2D, Double> labelPosition;

    public EdgeLayout(Edge edge, Point2D shift, Configuration config) {
        this.config = config;
        LNode[] lnodes = edge.getLNodes();

        points.add(new Point2D.Double(lnodes[0].getOutEdgeXPos(lnodes[1]) + shift.getX(),
                        lnodes[0].getLayer() * config.getLayerDistance() + 0.5*lnodes[0].getHeight() + shift.getY()));
        for (int i = 1; i < lnodes.length - 1; i++) {
            points.add(new Point2D.Double(lnodes[i].getPixelOffset() + shift.getX(),
                       lnodes[i].getLayer() * config.getLayerDistance() + shift.getY()));
        }
        points.add(new Point2D.Double(lnodes[lnodes.length-1].getInEdgeXPos(lnodes[lnodes.length - 2]) + shift.getX(),
    lnodes[lnodes.length-1].getLayer()*config.getLayerDistance()-0.5*lnodes[lnodes.length-1].getHeight()+shift.getY()));

        if (edge.isReversed()) {
            Collections.reverse(points);
        }
        Triple<Integer, Double, Boolean> t = edge.getLabelPos();
        Point2D position;
        double angle;
        if (t.third) {
            position = points.get(t.first);
            angle = Math.atan2(points.get(t.first + 1).getY() - points.get(t.first - 1).getY(), points.get(t.first+1).getX() - points.get(t.first-1).getX());
        } else {
            Point2D c1;
            Point2D c2;
            if (config.getUseBezierCurves()) {
                if (t.first == 0) {
                    c1 = Util.translatePoint(points.get(0), Util.scalePoint(Util.getDelta(points.get(0), points.get(1)), 2 * config.getControlPointDistance()));
                } else {
                    c1 = Util.translatePoint(points.get(t.first), Util.scalePoint(Util.getDelta(points.get(t.first-1), points.get(t.first+1)), config.getControlPointDistance()));
                }
                if (t.first == points.size() - 2) {
                    c2 = Util.translatePoint(points.get(t.first + 1), Util.scalePoint(Util.getDelta(points.get(t.first), points.get(t.first + 1)), -2 * config.getControlPointDistance()));
                } else {
                    c2 = Util.translatePoint(points.get(t.first+1), Util.scalePoint(Util.getDelta(points.get(t.first), points.get(t.first+2)), -config.getControlPointDistance()));
                }
                position = Util.bezier(points.get(t.first), c1, c2, points.get(t.first + 1), t.second);
                Point2D direction = Util.bezierDerivative(points.get(t.first), c1, c2, points.get(t.first + 1), t.second);
                angle = Math.atan2(direction.getY(), direction.getX());
            } else {
                position = Util.translatePoint(points.get(t.first),
                        Util.scalePoint(Util.getDelta(points.get(t.first), points.get(t.first + 1)), t.second));
                angle = Math.atan2(points.get(t.first + 1).getY() - points.get(t.first).getY(),
                        points.get(t.first + 1).getX() - points.get(t.first).getX());
            }
        }
        if (angle > Math.PI * 0.5 && angle < Math.PI * 1.5) angle -= Math.PI;
        labelPosition = new Pair<Point2D, Double>(position, angle);
    }

    public ArrayList<Point2D> getPoints() {
        return points;
    }

    public Point2D getStart() {
        return points.get(0);
    }

    public Point2D getEnd() {
        return points.get(points.size() - 1);
    }

    public Pair<Point2D, Double> getLabelPosition() {
        return labelPosition;
    }
}
