package io.github.stekeblad.videouploader.extensions.jfx;

/**
 * Class to hold information about the minimum and maximum allowed width/height of a window
 */
public class WindowDimensionsRestriction {
    public WindowDimensionsRestriction(double minW, double maxW, double minH, double maxH) {
        this.setMinW(minW);
        this.setMaxW(maxW);
        this.setMinH(minH);
        this.setMaxH(maxH);
    }

    private double minW;
    private double maxW;
    private double minH;
    private double maxH;

    public double getMinW() {
        return minW;
    }

    public void setMinW(double minW) {
        this.minW = minW;
    }

    public double getMaxW() {
        return maxW;
    }

    public void setMaxW(double maxW) {
        this.maxW = maxW;
    }

    public double getMinH() {
        return minH;
    }

    public void setMinH(double minH) {
        this.minH = minH;
    }

    public double getMaxH() {
        return maxH;
    }

    public void setMaxH(double maxH) {
        this.maxH = maxH;
    }
}