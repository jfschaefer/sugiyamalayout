package de.jfschaefer.sugiyamalayout;

import java.util.*;


/*
    An LGraph is a Graph representation that focuses on the different layers of the layered graph representation.
    Each layer is a list of LNodes, which can be dummy nodes for longer edges.
    The main goal is to permute the nodes in the different layers in a way that minimizes edge crossings.
    Also, this representation supports to get concrete pixel positions of the nodes.
 */
class LGraph {
    private Configuration config;
    private int numberOfLayers = 0;
    private ArrayList<Layer> layers = new ArrayList<Layer>();

    LGraph(Configuration configuration) {
        config = configuration;
    }

    void insertNode(LNode node) {
        node.setConfiguration(config);
        int layer = node.getLayer();
        while (layer >= numberOfLayers) {
            Layer nl = new Layer(config);
            if (numberOfLayers > 0) {
                nl.setParentLayer(layers.get(layers.size() - 1));
                layers.get(layers.size() - 1).setChildrenLayer(nl);
            }
            layers.add(nl);
            numberOfLayers++;
        }
        layers.get(layer).addNode(node);
    }

    void topDownParentMedianReordering() {
        for (int y = 1; y < numberOfLayers; y++) {
            layers.get(y).parentMedianReordering();
        }
    }

    void bottomUpMedianReordering() {
        for (int y = numberOfLayers - 1; y >= 0; y--) {
            layers.get(y).medianReordering();
        }
    }

    void topDownMedianReordering() {
        for (int y = 0; y < numberOfLayers; y++) {
            layers.get(y).medianReordering();
        }
    }

    void topDownAMReordering() {
        for (int y = 0; y < numberOfLayers; y++) {
            layers.get(y).arithmeticMeanReordering();
        }
    }

    void setInitialPixelOffsets() {
        for (Layer layer : layers) {
            layer.setInitialPixelOffsets();
        }
    }

    void topDownOffsetRelaxation() {
        for (Layer layer : layers) {
            layer.relaxPixelOffsets();
        }
    }

    boolean topDownGreedySwapping() {
        boolean somethingChanged = false;
        for (Layer layer : layers) {
            somethingChanged |= layer.greedyOptimization();
        }
        return somethingChanged;
    }

    void bottomUpOffsetRelaxation() {
        for (int i = 0; i < layers.size(); i++) {
            layers.get(layers.size() - 1 - i).relaxPixelOffsets();
        }
    }
}
