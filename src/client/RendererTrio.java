package client;

import line.ColoredDDAZBuff;
import line.LineRenderer;
import polygon.ColoredpolygonRenderer;
import polygon.PolygonRenderer;
import polygon.WireframeRenderer;

public class RendererTrio {
    private LineRenderer lineRenderer = ColoredDDAZBuff.make();
    private PolygonRenderer filledRenderer = ColoredpolygonRenderer.make();
    private PolygonRenderer wireFrameRenderer = WireframeRenderer.make();

    public LineRenderer getLineRenderer(){
        return lineRenderer;
    }

    public PolygonRenderer getFilledRenderer(){
        return filledRenderer;
    }

    public PolygonRenderer getWireframeRenderer(){
        return wireFrameRenderer;
    }
}
