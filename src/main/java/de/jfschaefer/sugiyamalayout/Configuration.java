package de.jfschaefer.sugiyamalayout;

public class Configuration implements java.io.Serializable {
    private double dummyNodeWidth = 25;
    private double initialNodeMargin = 121;
    private double minimalNodeMargin = 36;
    private double layerDistance = 121;
    private double graphPadding = 25;
    private boolean centralizeEdges = false;
    private boolean useBeziercurves = true;


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

    public double getMinimalNodeMargin() {
        return minimalNodeMargin;
    }

    public void setMinimalNodeMargin(double margin) {
        minimalNodeMargin = margin;
    }

    public boolean getCentralizeEdges() {
        return centralizeEdges;
    }

    public void setCentralizeEdges (boolean edgeCentralization) {
        this.centralizeEdges = edgeCentralization;
    }

    public boolean getUseBezierCurves() {
        return useBeziercurves;
    }

    public void setUseBezierCurves(boolean useBeziercurves) {
        this.useBeziercurves = useBeziercurves;
    }
}
