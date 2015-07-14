package de.jfschaefer.sugiyamalayout;

import java.awt.geom.Point2D;
import java.util.*;

public class LatexGenerator<V, E> {
    private StringBuffer result = new StringBuffer();
    public LatexGenerator(Layout<V, E> layout, Map<V, String> nodeNames) {
        result.append("\\begin{tikzpicture}\n");
        // Step 1: Draw nodes
        for (V node : layout.getNodeSet()) {
            Point2D pos = layout.getNodeCenter(node);
            result.append("\\node (rect) at (");
            result.append(pos.getX());
            result.append("pt, ");
            result.append(pos.getY());
            result.append("pt) [draw, minimum width=");
            result.append(layout.getNodeWidth(node));
            result.append("pt, minimum height=");
            result.append(layout.getNodeHeight(node));
            result.append("pt] {");
            result.append(nodeNames.get(node));
            result.append("};\n");
        }

        // Step 2: Draw edges   TODO: draw edge labels; maybe use dummy nodes only as control points
        for (E edge : layout.getEdgeSet()) {
            EdgeLayout el = layout.getEdgeLayout(edge);
            ArrayList<Point2D> points = el.getPoints();
            for (int i = 0; i < points.size() - 1; i++) {
                result.append("\\draw");
                if (i == points.size() - 2) {
                    result.append("[->]");
                }
                result.append(" (");
                result.append(points.get(i).getX());
                result.append("pt, ");
                result.append(points.get(i).getY());
                result.append("pt) -- (");
                result.append(points.get(i+1).getX());
                result.append("pt, ");
                result.append(points.get(i+1).getY());
                result.append("pt);\n");
            }
        }

        result.append("\\end{tikzpicture}\n");
    }

    public String getLatex() {
        return result.toString();
    }
}
