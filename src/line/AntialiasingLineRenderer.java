package line;

import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

/*
 * LC: modified at 1:12 AM 9/28/2017
 * */
public class AntialiasingLineRenderer implements LineRenderer{
    private static final double AREA = Math.PI * Math.pow(0.5, 2);
    private static final double RADIUS = 0.5;
    private static final double DIAMETER = 1.0;

    
    private AntialiasingLineRenderer(){}

    @Override
    public void drawLine(Vertex3D p1, Vertex3D p2, Drawable panel) {
        render(p1.rounded(), p2.rounded(), panel);
    }



    private double Distance(double xPixel, double yPixel, Vertex3D p1, Vertex3D p2){
        double dx = p2.getIntX() - p1.getIntX();
        double dy = p2.getIntY() - p1.getIntY();
        double angle = Math.toRadians((int)(Math.toDegrees(Math.atan(dy/ dx))));
        double y = p1.getIntY();
        double x = p1.getIntX();
        while(x <= xPixel){
             x++;
             y += dy/dx;
        }
        double distance = Math.abs(yPixel - y);
        return (Math.cos(angle) * distance);
    }

    private void render(Vertex3D p1, Vertex3D p2, Drawable panel) {
        for (int i = p1.getIntY() - 1; i < p2.getIntY() + 1; i++) {
            for (int j = p1.getIntX() - 1; j < p2.getIntX() + 1; j++) {
                double d = Distance(j, i, p1.rounded(), p2.rounded());
                Color oldColor = Color.fromARGB(panel.getPixel(j, i));
                Color color = p1.getColor().blendInto(Proportion(d), oldColor);
                if(oldColor.asARGB() < color.asARGB()){
                    panel.setPixel(j, i, 0.0, color.asARGB());
                }
            }
        }
    }
    
    private double Proportion(double distance){
        if(distance >= DIAMETER){
            return 0.0;
        }else if(distance == RADIUS){
            return 0.5;
        }else if(distance == 0.0) {
            return 1.0;
        }else if(distance > RADIUS && distance < DIAMETER) {
            double theta = Math.acos((distance - RADIUS)/ RADIUS);
            double Triangle = (distance - RADIUS) * (Math.sqrt(Math.pow(RADIUS, 2) - Math.pow((distance - RADIUS), 2)));
            return ((theta / Math.PI) * AREA - Triangle) / AREA;
        }else{
            double Triangle = (RADIUS - distance) * (Math.sqrt(Math.pow(RADIUS, 2) - Math.pow((RADIUS - distance), 2)));
            return ((1 - (Math.acos((RADIUS - distance)/ RADIUS) / Math.PI)) * AREA + Triangle) / AREA;
        }
    }

    public static LineRenderer make(){
        return new AnyOctantLineRenderer(new AntialiasingLineRenderer());
    }
}

