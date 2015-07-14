package de.jfschaefer.sugiyamalayout;

import java.awt.geom.Point2D;
import java.util.*;

// While the node layout is pretty much determined by the center point,
// the edge layout is more complicated, so it gets its own class
// TODO: Extend support for label positions and (if useful) for e.g. B-Spline visualization
public class EdgeLayout {
    private Configuration config;
    private ArrayList<Point2D> points = new ArrayList<Point2D>();

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
}
