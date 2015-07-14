package de.jfschaefer.sugiyamalayout.visualizationFX;

import de.jfschaefer.sugiyamalayout.EdgeLayout;
import de.jfschaefer.sugiyamalayout.Layout;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.Node;

import java.awt.geom.Point2D;
import java.util.*;

public class GraphFX<V, E> extends Pane {
    public GraphFX(Layout<V, E> layout, GraphFXNodeFactory<V> nodeFactory) {
        minWidth(layout.getWidth());
        maxWidth(layout.getWidth());
        minHeight(layout.getHeight());
        maxHeight(layout.getHeight());

        for (E edge: layout.getEdgeSet()) {
            EdgeLayout el = layout.getEdgeLayout(edge);
            ArrayList<Point2D> points = el.getPoints();
            for (int i = 0; i < points.size() - 1; i++) {
                Line segment = new Line(points.get(i).getX(), points.get(i).getY(),
                                        points.get(i+1).getX(), points.get(i+1).getY());
                getChildren().add(segment);
            }
        }

        for (V node: layout.getNodeSet()) {
            Point2D pos = layout.getNodeTopLeft(node);
            Node newNode = nodeFactory.getNodeVisualization(node,
                    layout.getNodeWidth(node), layout.getNodeHeight(node));
            newNode.setLayoutX(pos.getX());
            newNode.setLayoutY(pos.getY());
            getChildren().add(newNode);
        }
    }
}
