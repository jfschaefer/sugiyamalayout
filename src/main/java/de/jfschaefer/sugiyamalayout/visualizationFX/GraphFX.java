package de.jfschaefer.sugiyamalayout.visualizationFX;

import de.jfschaefer.sugiyamalayout.Configuration;
import de.jfschaefer.sugiyamalayout.EdgeLayout;
import de.jfschaefer.sugiyamalayout.Layout;
import de.jfschaefer.sugiyamalayout.Util;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.CubicCurve;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.shape.Polygon;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;

import java.awt.geom.Point2D;
import java.util.*;

public class GraphFX<V, E> extends Pane {
    public GraphFX(Layout<V, E> layout, GraphFXNodeFactory<V> nodeFactory, Map<E, String> edgeLabels) {
        Configuration config = layout.getConfig();
        minWidth(layout.getWidth());
        maxWidth(layout.getWidth());
        minHeight(layout.getHeight());
        maxHeight(layout.getHeight());

        for (E edge: layout.getEdgeSet()) {
            final EdgeLayout el = layout.getEdgeLayout(edge);
            ArrayList<Point2D> points = el.getPoints();
            if (layout.getConfig().getUseBezierCurves()) {
                for (int i = 0; i < points.size() - 1; i++) {
                    Point2D c0;
                    if (i == 0) {
                        c0 = Util.translatePoint(points.get(0), Util.scalePoint(Util.getDelta(points.get(0), points.get(1)), 2 * config.getControlPointDistance()));
                    } else {
                        c0 = Util.translatePoint(points.get(i), Util.scalePoint(Util.getDelta(points.get(i-1), points.get(i+1)), config.getControlPointDistance()));
                    }
                    Point2D c1;
                    if (i == points.size() - 2) {
                        c1 = Util.translatePoint(points.get(i+1), Util.scalePoint(Util.getDelta(points.get(i), points.get(i+1)), -2*config.getControlPointDistance()));
                        if (layout.getConfig().getDrawArrowHeads()) {
                            Polygon arrowhead = new Polygon(0d, 0d, 4d, 8d, -4d, 8d);
                            double angle = Math.atan2(points.get(i+1).getY() - points.get(i).getY(),
                                    points.get(i+1).getX() - points.get(i).getX());
                            arrowhead.setLayoutX(points.get(i+1).getX());
                            arrowhead.setLayoutY(points.get(i+1).getY());
                            arrowhead.getTransforms().add(new Rotate(365d / (2 * Math.PI) * angle + 90, 0, 0));
                            getChildren().add(arrowhead);
                        }
                    } else {
                        c1 = Util.translatePoint(points.get(i+1), Util.scalePoint(Util.getDelta(points.get(i), points.get(i+2)), -config.getControlPointDistance()));
                    }
                    CubicCurve segment = new CubicCurve(points.get(i).getX(), points.get(i).getY(), c0.getX(), c0.getY(),
                            c1.getX(), c1.getY(), points.get(i+1).getX(), points.get(i+1).getY());
                    segment.setFill(Color.TRANSPARENT);
                    segment.setStroke(Color.BLACK);
                    getChildren().add(segment);
                }
            } else {
                for (int i = 0; i < points.size() - 1; i++) {
                    Line segment = new Line(points.get(i).getX(), points.get(i).getY(),
                            points.get(i+1).getX(), points.get(i+1).getY());
                    getChildren().add(segment);
                }
            }
            if (config.getDrawLabels()) {
                Label label = new Label(edgeLabels.get(edge));
                label.setStyle("-fx-font-size: 0.8em;");
                final Label l2 = label;
                label.boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
                    @Override
                    public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                        l2.getTransforms().clear();
                        l2.setLayoutX(el.getLabelPosition().first.getX() - 0.5 * newValue.getWidth());
                        l2.setLayoutY(el.getLabelPosition().first.getY() - l2.getHeight());
                        l2.getTransforms().add(new Rotate(365d / (2 * Math.PI) * el.getLabelPosition().second, l2.getWidth() * 0.5, l2.getHeight()));
                    }
                });
                label.setLayoutX(el.getLabelPosition().first.getX() - label.getWidth()*0.5);
                label.setLayoutY(el.getLabelPosition().first.getY() - label.getHeight());
                label.getTransforms().add(new Rotate(365d / (2 * Math.PI) * el.getLabelPosition().second, label.getWidth() * 0.5, label.getHeight()));
                getChildren().add(label);
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
