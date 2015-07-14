package de.jfschaefer.sugiyamalayout.visualizationFX;

import javafx.scene.Node;

public interface GraphFXNodeFactory<V> {
    public Node getNodeVisualization(V node, double width, double height);
}
