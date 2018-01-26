package windowing.drawable;

import client.interpreter.SimpInterpreter;

public class ClippingDrawableDecorator extends DrawableDecorator {

    private double xLow, xHigh, yLow, yHigh;

    public ClippingDrawableDecorator(Drawable delegate, double xLow, double xHigh, double yLow, double yHigh, double zLow, double zHigh) {
        super(delegate);

        this.xLow = xLow;
        this.xHigh = xHigh;
        this.yLow = yLow;
        this.yHigh = yHigh;
    }

    private Boolean pixelInDrawRange(double x, double y, double z){
    	xLow = (delegate.getWidth()-SimpInterpreter.getWidth())/2;
    	yLow = (delegate.getHeight()-SimpInterpreter.getHeight())/2;
    	xHigh = delegate.getWidth() - xLow;
    	yHigh = delegate.getHeight() - yLow;
        return (x >= this.xLow && x < this.xHigh) && (y >= this.yLow && y < this.yHigh);
    }

    @Override
    public void setPixel(int x, int y, double z, int argbColor) {
        if (pixelInDrawRange(x, y, z)) {delegate.setPixel(x, y, z, argbColor);}
    }

    @Override
    public void setPixelWithCoverage(int x, int y, double z, int argbColor, double coverage) {
        if (pixelInDrawRange(x, y, z)) {delegate.setPixelWithCoverage(x,  y, z, argbColor, coverage);}
    }
}
