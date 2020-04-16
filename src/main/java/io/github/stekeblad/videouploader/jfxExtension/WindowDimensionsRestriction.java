package io.github.stekeblad.videouploader.jfxExtension;

public class WindowDimensionsRestriction {
    public WindowDimensionsRestriction(double minW, double maxW, double minH, double maxH) {
        this.minW = minW;
        this.maxW = maxW;
        this.minH = minH;
        this.maxH = maxH;
    }

    public double minW;
    public double maxW;
    public double minH;
    public double maxH;

}