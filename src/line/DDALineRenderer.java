package line;

import geometry.Vertex;
import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

public class DDALineRenderer implements LineRenderer {
	// use the static factory make() instead of constructor.
	private DDALineRenderer() {}
	
	/*
	 LC: modified at 9:03 9/26/2017
	 */
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {
		double deltaX = p2.getIntX() - p1.getIntX();
		double deltaY = p2.getIntY() - p1.getIntY();
		
		double slope = deltaY / deltaX;
		int x = p1.getIntX(); 
		double y = p1.getIntY();
		int argbColor = p1.getColor().asARGB();
		
		while(x <= p2.getIntX()) {
			drawable.setPixel(x, (int)Math.round(y), 0.0, argbColor);
			x++;
			y+=slope;
		}
	}
	
	public static LineRenderer make() {
		return new AnyOctantLineRenderer(new DDALineRenderer());
	}

}

