package de.jfschaefer.sugiyamalayout;

public class Configuration implements java.io.Serializable {
    private double dummyNodeWidth = 25;
    private double initialNodeMargin = 64;
    private double minimalNodeMargin = 16;
    private double layerDistance = 64;
    private double graphPadding = 25;
    private double controlPointDistance = 0.2;
    private boolean centralizeEdges = false;
    private boolean useBeziercurves = true;
    private boolean useAlternativeAlgorithm = false;    // otherwise algorithm closer to original Sugiyama will be used
    private boolean drawArrowHeads = true;
    private boolean drawLabels = true;


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

    public double getControlPointDistance() {
        return controlPointDistance;
    }

    public void setControlPointDistance(double distance) {
        controlPointDistance = distance;
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

    public void setUseAlternativeAlgorithm(boolean useAlternativeAlgorithm) {
        this.useAlternativeAlgorithm = useAlternativeAlgorithm;
    }

    public boolean getUseAlternativeAlgorithm() {
        return useAlternativeAlgorithm;
    }

    public boolean getDrawArrowHeads() {
        return drawArrowHeads;
    }

    public void setDrawArrowHeads(boolean drawArrowHeads) {
        this.drawArrowHeads = drawArrowHeads;
    }

    public boolean getDrawLabels() {
        return drawLabels;
    }

    public void setDrawLabels(boolean drawLabels) {
        this.drawLabels = drawLabels;
    }
}
