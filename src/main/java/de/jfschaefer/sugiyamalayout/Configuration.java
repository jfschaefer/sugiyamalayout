package de.jfschaefer.sugiyamalayout;

public class Configuration implements java.io.Serializable {
    private double dummyNodeWidth = 24;
    private double initialNodeMargin = 52;
    private double layerDistance = 124;
    private double graphPadding = 24;
    private boolean edgeCentralization = false;


    public double getDummyNodeWidth() {
        return dummyNodeWidth;
    }

    public void setDummyNodeWidth(double width) {
        assert width >= 0;
        dummyNodeWidth = width;
    }


    public double getInitialNodeMargin() {
        return initialNodeMargin;
    }

    public void setInitialNodeMargin(double margin) {
        initialNodeMargin = margin;
    }

    public double getLayerDistance() {
        return layerDistance;
    }

    public void setLayerDistance(double distance) {
        layerDistance = distance;
    }

    public double getGraphPadding() {
        return graphPadding;
    }

    public void setGraphPadding(double padding) {
        graphPadding = padding;
    }

    public boolean isEdgeCentralization() {
        return edgeCentralization;
    }

    public void setEdgeCentralization(boolean edgeCentralization) {
        this.edgeCentralization = edgeCentralization;
    }
}
