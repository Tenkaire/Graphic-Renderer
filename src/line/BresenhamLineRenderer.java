package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;

public class BresenhamLineRenderer implements LineRenderer {
	// use the static factory make() instead of constructor.
	private BresenhamLineRenderer() {}

	
	/*
	 LC: modified at 9/24/2017
	 */
	@Override
	public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable) {		
		int m_num=2*(p2.getIntY()-p1.getIntY());
		int x=p1.getIntX();
		int y=p1.getIntY();
		int error=(p2.getIntY()-p1.getIntY())*2-(p2.getIntX()-p1.getIntX());
		int k=(p2.getIntY()-p1.getIntY())*2-(p2.getIntX()-p1.getIntX())*2;
		int argbColor = p1.getColor().asARGB();		
		while(x<=p2.getIntX()){
			drawable.setPixel(x, y, 0.0, argbColor);
			x++;
			if(error>=0){
				error+=k;
				y++;
			}
			else{
				error+=m_num;
			}
		}

	}

	public static LineRenderer make() {
		return new AnyOctantLineRenderer(new BresenhamLineRenderer());
	}


}
