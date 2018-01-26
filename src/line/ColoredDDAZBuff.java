package line;

import client.interpreter.SimpInterpreter;
import geometry.Vertex3D;
import windowing.drawable.Drawable;
import windowing.graphics.Color;


public class ColoredDDAZBuff implements LineRenderer {
    private ColoredDDAZBuff(){}

    public static LineRenderer make() {return new AnyOctantLineRenderer(new ColoredDDAZBuff());}

    @Override
    public void drawLine(Vertex3D p1, Vertex3D p2, Drawable drawable){

        LinearInterpolator y = new LinearInterpolator(p1.getX(), p2.getX(), p1.getX(), p1.getY(), p2.getY());
        LinearInterpolator z = new LinearInterpolator(p1.getX(), p2.getX(), p1.getX(), 1 / p1.getZ(), 1 / p2.getZ());
        LinearInterpolator red = new LinearInterpolator(p1.getX(), p2.getX(), p1.getX(), p1.getColor().getR() / p1.getZ(), p2.getColor().getR() / p2.getZ());
        LinearInterpolator green = new LinearInterpolator(p1.getX(), p2.getX(), p1.getX(), p1.getColor().getG() / p1.getZ(), p2.getColor().getG() / p2.getZ());
        LinearInterpolator blue = new LinearInterpolator(p1.getX(), p2.getX(), p1.getX(), p1.getColor().getB() / p1.getZ(), p2.getColor().getB() / p2.getZ());

        for (int x = p1.getIntX(); x < p2.getIntX(); x++){
            double realZ = 1 / z.getValue();
            double realRed = realZ * red.getValue();
            double realGreen = realZ * green.getValue();
            double realBlue = realZ * blue.getValue();

            Color color = new Color(realRed, realGreen, realBlue);

//            if(x >= 0 && x <= 650 && y.getValue() >= 0 && y.getValue() <= 650){
//                if(z.getValue() >= SimpInterpreter.zBuffering.getValue(x, (int) Math.round(y.getValue()))) {
            drawable.setPixel(x, (int) Math.round(y.getValue()), realZ, color.asARGB());
//                    SimpInterpreter.zBuffering.changeValue(x, (int) Math.round(y.getValue()), z.getValue());
//                }
//            }
//
            y.increment();
            z.increment();
            red.increment();
            green.increment();
            blue.increment();
        }
    }
}
