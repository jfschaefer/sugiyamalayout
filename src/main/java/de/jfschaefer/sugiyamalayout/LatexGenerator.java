package de.jfschaefer.sugiyamalayout;

import java.awt.geom.Point2D;
import java.util.*;

public class LatexGenerator<V, E> {
    private StringBuffer result = new StringBuffer();
    public LatexGenerator(Layout<V, E> layout, Map<V, String> nodeNames, Map<E, String> edgeLabels, Configuration config) {
        result.append("\\begin{tikzpicture}\n");
        // Step 1: Draw nodes
        for (V node : layout.getNodeSet()) {
            Point2D pos = layout.getNodeCenter(node);
            result.append("\\node (rect) at (");
            result.append(pos.getX());
            result.append("pt, ");
            result.append(layout.getHeight() - pos.getY());
            result.append("pt) [draw, minimum width=");
            result.append(layout.getNodeWidth(node));
            result.append("pt, minimum height=");
            result.append(layout.getNodeHeight(node));
            if (layout.nodeHasMarker(node)) {
                result.append("pt, fill=red] {");
            } else {
                result.append("pt] {");
            }
            result.append(latexEscape(nodeNames.get(node)));
            result.append("};\n");
        }

        // Step 2: Draw edges   TODO: draw edge labels; maybe use dummy nodes only as control points
        for (E edge : layout.getEdgeSet()) {
            EdgeLayout el = layout.getEdgeLayout(edge);
            ArrayList<Point2D> points = el.getPoints();
            for (int i = 0; i < points.size() - 1; i++) {
                result.append("\\draw");
                if (config.getDrawArrowHeads() && i == points.size() - 2) {
                    result.append("[->]");
                }
                result.append(" (");
                result.append(points.get(i).getX());
                result.append("pt, ");
                result.append(layout.getHeight() - points.get(i).getY());
                result.append("pt)");
                if (!config.getUseBezierCurves()) {
                    result.append(" -- ");
                } else {
                    result.append(" .. controls (");
                    Point2D c0;
                    if (i == 0) {
                        c0 = Util.translatePoint(points.get(0), Util.scalePoint(Util.getDelta(points.get(0), points.get(1)), config.getControlPointDistance() * 2));
                    } else {
                        c0 = Util.translatePoint(points.get(i), Util.scalePoint(Util.getDelta(points.get(i-1), points.get(i+1)), config.getControlPointDistance()));
                    }
                    result.append(c0.getX());
                    result.append("pt, ");
                    result.append(layout.getHeight() - c0.getY());
                    result.append("pt) and (");
                    Point2D c1;
                    if (i == points.size() - 2) {
                        c1 = Util.translatePoint(points.get(i+1), Util.scalePoint(Util.getDelta(points.get(i), points.get(i + 1)), -config.getControlPointDistance() * 2));
                    } else {
                        c1 = Util.translatePoint(points.get(i+1), Util.scalePoint(Util.getDelta(points.get(i), points.get(i+2)), -config.getControlPointDistance()));
                    }
                    result.append(c1.getX());
                    result.append("pt, ");
                    result.append(layout.getHeight() - c1.getY());
                    result.append("pt) .. ");
                }
                result.append("(");
                result.append(points.get(i+1).getX());
                result.append("pt, ");
                result.append(layout.getHeight() - points.get(i+1).getY());
                result.append("pt);\n");
            }

            if (config.getDrawLabels()) {
                result.append("\\node [rotate=");
                result.append(-el.getLabelPosition().second * 365 / (2 * Math.PI));
                result.append(", align=left] at (");    //, fill=white, opacity=0.7
                result.append(el.getLabelPosition().first.getX());
                result.append("pt, ");
                result.append(layout.getHeight() - el.getLabelPosition().first.getY());
                result.append("pt) {");
                result.append(latexEscape(edgeLabels.get(edge)));
                result.append("\\\\};\n");
            }
        }

        result.append("\\end{tikzpicture}\n");
    }


    public static String latexEscape(String s) {
        return s.replace("_", "\\_").replace("{", "\\").replace("}", "\\}");    //TODO: Do proper escaping
    }

    public String getLatex() {
        return result.toString();
    }
}
