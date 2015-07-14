package de.jfschaefer.sugiyamalayout;

import java.awt.geom.Point2D;
import java.util.*;

public class Layout<V, E> implements java.io.Serializable {
    private Configuration config;
    private double minX, minY, maxX, maxY;
    private double shiftX, shiftY;
    private double width, height;

    //node -> (Center, TopLeft)
    private Map<V, Pair<Point2D, Point2D>> nodePositions = new HashMap<V, Pair<Point2D, Point2D>>();
    private Map<E, EdgeLayout> edgePositions = new HashMap<E, EdgeLayout>();
    public Layout(LGraph lgraph, Map<V, Node> nodes, Map<E, Edge> edges, Configuration configuration) {
        config = configuration;

        boolean initializedBoundaryValues = false;
        for (Map.Entry<V, Node> entry : nodes.entrySet()) {
            Node n = entry.getValue();
            Point2D center = new Point2D.Double(n.getLNode().getPixelOffset(), n.getLayer()*config.getLayerDistance());
            Point2D topleft = new Point2D.Double(n.getLNode().getLeftPixelOffset(), center.getY() - n.getHeight()*0.5);

            nodePositions.put(entry.getKey(), new Pair<Point2D, Point2D>(center, topleft));

            double minx = topleft.getX();
            double maxx = center.getX() + n.getWidth()*0.5;
            double miny = topleft.getY();
            double maxy = center.getY() + 0.5 * n.getHeight();
            if (!initializedBoundaryValues) {
                initializedBoundaryValues = true;
                minX = minx;
                minY = miny;
                maxX = maxx;
                maxY = maxy;
            }
            if (minx < minX) minX = minx;
            if (maxx > maxX) maxX = maxx;
            if (miny < minY) minY = miny;
            if (maxy > maxY) maxY = maxy;
        }

        shiftX = -minX + config.getGraphPadding();
        shiftY = -minY + config.getGraphPadding();

        width = maxX - minX + 2 * config.getGraphPadding();
        height = maxY - minY + 2 * config.getGraphPadding();

        for (Map.Entry<E, Edge> entry : edges.entrySet()) {
            Edge e = entry.getValue();
            EdgeLayout el = new EdgeLayout(e, new Point2D.Double(shiftX, shiftY), config);
            edgePositions.put(entry.getKey(), el);
        }
    }

    public Point2D getNodeCenter(V node) {
        return Util.translatePoint(nodePositions.get(node).first, new Point2D.Double(shiftX, shiftY));
    }

    public Point2D getNodeTopLeft(V node) {
        return Util.translatePoint(nodePositions.get(node).second, new Point2D.Double(shiftX, shiftY));
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
