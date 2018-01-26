package polygon;

import geometry.Vertex3D;
import windowing.drawable.Drawable;

/*
 * modified at 3:43pm 10/31/2017
 * */
public class FilledPolygonRenderer implements PolygonRenderer{
    private FilledPolygonRenderer() {}

    public static PolygonRenderer make() {return new FilledPolygonRenderer();}

    @Override
    public void drawPolygon(Polygon polygon, Drawable panel, Shader vertexShader) {
        Chain left_chain = polygon.leftChain();
        if(left_chain.get(0).getIntY() != left_chain.get(1).getIntY()) {
            fillPolygon(polygon, panel);
        }else if(left_chain.get(0).getIntY() == left_chain.get(1).getIntY()) {
            x_same_triangle(polygon, panel);
        }
    }

    private void x_same_triangle(Polygon polygon, Drawable panel) {
        Chain left_chain = polygon.leftChain();
//        Vertex3D pLeft;
//        Vertex3D pRight;
//        Vertex3D pBottom;
//
//        if(polygon.get(0).getX() == polygon.get(1).getX() ||
//                polygon.get(0).getX() == polygon.get(2).getX() ||
//                polygon.get(1).getX() == polygon.get(2).getX()) {
//            pLeft = left_chain.get(0);
//            pRight = left_chain.get(1);
//            pBottom = left_chain.get(2);
//        }
//        else {
//            pRight = left_chain.get(0);
//            pLeft = left_chain.get(1);
//            pBottom = left_chain.get(2);
//        }

        Vertex3D pLeft = null;
        Vertex3D pRight = null;
        Vertex3D pBottom = null;

        Polygon.DeterminePoint(polygon,pLeft,pRight,pBottom,left_chain);

        double xLeft = pLeft.getIntX();
        double xRight = pRight.getIntX();
        double xBottom = pBottom.getIntX();
        double yTop = pLeft.getIntY();
        double yBottom = pBottom.getIntY();
        double dL = xBottom - xLeft;
        double dR = xBottom - xRight;
        double dy = yTop - yBottom;
        double slope_left = dL / dy;
        double slope_right = dR / dy;

        double x_L = xLeft;
        double x_R = xRight;
        for(double y = yTop; y > yBottom; y--) {
            for(double x = x_L; x < x_R; x++) {
                panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, pLeft.getColor().asARGB());
            }
            x_L += slope_left;
            x_R += slope_right;
        }
    }

    private void fillPolygon(Polygon polygon, Drawable panel) {
        Chain left_chain = polygon.leftChain();
        Chain right_chain = polygon.rightChain();
        int count_left = left_chain.length();
        if(count_left == 2) {
            Vertex3D pTop = left_chain.get(0);
            Vertex3D pBottom = left_chain.get(1);
            Vertex3D pMiddle = right_chain.get(1);

            int xTop = pTop.getIntX();
            int xMiddle = pMiddle.getIntX();
            int xBottom = pBottom.getIntX();
            int yTop = pTop.getIntY();
            int yMiddle = pMiddle.getIntY();
            int yBottom = pBottom.getIntY();
            double dL = pBottom.getIntX() - pTop.getIntX();
            double dy = pTop.getIntY() - pBottom.getIntY();
            double dXRUp = xMiddle - xTop;
            double dYRUp = yTop - yMiddle;
            double dXRLow = xBottom - xMiddle;
            double dYRLow = yMiddle - yBottom;
            double left_slope = dL / dy;
            double kUpperRight = dXRUp / dYRUp;
            double kLowerRight = dXRLow / dYRLow;
            double x = xTop;
            double xLeft = xTop;
            double xRight = xTop;
            double y = yTop;
            for(; y > yMiddle; y--) {
                for(; x < xRight ; x++) {
                    panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, pTop.getColor().asARGB());
                }
                xLeft += left_slope;
                xRight += kUpperRight;
                x = xLeft;
            }
            for(; y > yBottom; y--) {
                for(; x < xRight ; x++) {
                    panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, pTop.getColor().asARGB());
                }
                xLeft += left_slope;
                xRight += kLowerRight;
                x = xLeft;
            }
        }else if(count_left == 3){
            Vertex3D pTop = left_chain.get(0);
            Vertex3D pMiddle = left_chain.get(1);
            Vertex3D pBottom = right_chain.get(1);

            int xTop = pTop.getIntX();
            int xMiddle = pMiddle.getIntX();
            int xBottom = pBottom.getIntX();
            int yTop = pTop.getIntY();
            int yMiddle = pMiddle.getIntY();
            int yBottom = pBottom.getIntY();
            double dX = xBottom - xTop;
            double dY = yTop - yBottom;
            double dXLUp = xMiddle - xTop;
            double dYLUp = yTop - yMiddle;
            double dXLLow = xBottom - xMiddle;
            double dYLLow = yMiddle - yBottom;
            double right_slope = dX / dY;
            double kUpperLeft = dXLUp / dYLUp;
            double kLowerLeft = dXLLow / dYLLow;
            double x = xTop;
            double y = yTop;
            double xLeft = xTop;
            double xRight = xTop;

            for(; y > yMiddle; y--) {
                for(; x < xRight; x++) {
                    panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, pTop.getColor().asARGB());
                }
                xLeft += kUpperLeft;
                xRight += right_slope;
                x = xLeft;
            }
            for(; y > yBottom; y--) {
                for(; x < xRight; x++) {
                    panel.setPixel((int)Math.round(x), (int)Math.round(y), 0, pTop.getColor().asARGB());
                }
                xLeft += kLowerLeft;
                xRight += right_slope;
                x = xLeft;
            }
        }

    }

}