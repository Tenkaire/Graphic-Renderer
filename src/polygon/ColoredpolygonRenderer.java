package polygon;

import client.interpreter.ObjReader;
import client.interpreter.SimpInterpreter;
import geometry.Vector;
import geometry.Vertex3D;
import client.PointInterpolation;
import shading.LightingCalculation;
import windowing.drawable.Drawable;
import windowing.graphics.Color;

import java.util.ArrayList;

public class ColoredpolygonRenderer implements PolygonRenderer {

    private enum SIDE {LEFT, RIGHT, BOTH, NEITHER}
    public static PolygonRenderer make(){return new ColoredpolygonRenderer();}

    @Override
    public void drawPolygon(Polygon polygon, Drawable panel, Shader vertexShader) {
        Chain leftChain = polygon.leftChain();
        Chain rightChain = polygon.rightChain();
        int leftIndex = leftChain.length() - 1;
        int rightIndex = rightChain.length() - 1;

        int p1LeftIndex = 0;
        int p1RightIndex = 0;
        int p2LeftIndex = 1;
        int p2RightIndex = 1;
        SIDE side = SIDE.NEITHER;

        while (p2LeftIndex < leftIndex || p2RightIndex <rightIndex){
            if ((side == SIDE.BOTH || side == SIDE.LEFT) && p2LeftIndex < leftIndex){
                p1LeftIndex++;p2LeftIndex++;
            }
            if ((side == SIDE.BOTH || side == SIDE.RIGHT) && p2RightIndex < rightIndex){
                p1RightIndex++;p2RightIndex++;
            }
            side = polygonRendering(panel, leftChain.get(p1LeftIndex), leftChain.get(p2LeftIndex),
                    rightChain.get(p1RightIndex), rightChain.get(p2RightIndex));
        }

    }

    private SIDE polygonRendering(Drawable panel, Vertex3D p1Left, Vertex3D p2Left, Vertex3D p1Right, Vertex3D p2Right) {
        ArrayList<Vertex3D> vertexList;
        if (SimpInterpreter.isHasNormal()) {
            vertexList = ObjReader.getList();
        } else {
            vertexList = SimpInterpreter.getVerticeList();
        }

        Vertex3D p1 = vertexList.get(0); // in cam space
        Vertex3D p2 = vertexList.get(1);
        Vertex3D p3 = vertexList.get(2);
        double originalYValue = Math.min(p1Left.getY(), p1Right.getY());
        double endY = Math.max(p2Left.getY(), p2Right.getY());
        if (SimpInterpreter.getShadingStyle() == SimpInterpreter.ShadingStyle.FLAT) {             // FLAT
            Vector normal = null;
            if (SimpInterpreter.isHasNormal()) {
                ArrayList<Vector> normals = ObjReader.getNormalList();
                double tempX = 0;
                double tempY = 0;
                double tempZ = 0;
                for (int i = 0; i < normals.size(); i++) {
                    tempX += normals.get(i).getX();
                    tempY += normals.get(i).getY();
                    tempZ += normals.get(i).getZ();
                }
                normal = new Vector(tempX / 3, tempY / 3, tempZ / 3).normalize();
            } else {
                normal = new Vector(p2.getPoint3D(), p3.getPoint3D()).crossProduct(new Vector(p2.getPoint3D(), p1.getPoint3D()));
            }
            normal = normal.normalize();
            Vertex3D center_point = new Vertex3D((p1.getX() + p2.getX() + p3.getX()) / 3, (p1.getY() + p2.getY() + p3.getY()) / 3,
                    (p1.getZ() + p2.getZ() + p3.getZ()) / 3, Color.WHITE);
            LightingCalculation light_obj = new LightingCalculation(SimpInterpreter.getLightColor(), SimpInterpreter.getAttA(), SimpInterpreter.getAttB(), SimpInterpreter.getAmbientLight(),
                    SimpInterpreter.getKd(), SimpInterpreter.getLightLocation(), normal, SimpInterpreter.getKs(), SimpInterpreter.getP(), center_point);
            Color c = light_obj.getLightingColor();
            p1Left = p1Left.replaceColor(p1Left, c);
            p2Left = p2Left.replaceColor(p2Left, c);
            p1Right = p1Right.replaceColor(p1Right, c);
            p2Right = p2Right.replaceColor(p2Right, c);
        } else if (SimpInterpreter.getShadingStyle() == SimpInterpreter.ShadingStyle.GOURAUD) { // GOURAUD
            Vector normal_p3 = null;
            Vector normal_p2 = null;
            Vector normal_p1 = null;
            if (SimpInterpreter.isHasNormal()) {
                normal_p1 = p1.getNormal().normalize();
                normal_p2 = p2.getNormal().normalize();
                normal_p3 = p3.getNormal().normalize();
            } else {
                normal_p3 = new Vector(p3.getPoint3D(), p1.getPoint3D()).crossProduct(new Vector(p3.getPoint3D(),
                        p2.getPoint3D())).normalize();
                normal_p2 = new Vector(p2.getPoint3D(), p3.getPoint3D()).crossProduct(new Vector(p2.getPoint3D(),
                        p1.getPoint3D())).normalize();
                normal_p1 = new Vector(p1.getPoint3D(), p2.getPoint3D()).crossProduct(new Vector(p1.getPoint3D(),
                        p3.getPoint3D())).normalize();
            }

            LightingCalculation lightObjP1 = new LightingCalculation(SimpInterpreter.getLightColor(), SimpInterpreter.getAttA(),
                    SimpInterpreter.getAttB(), SimpInterpreter.getAmbientLight(),
                    SimpInterpreter.getKd(), SimpInterpreter.getLightLocation(), normal_p1, SimpInterpreter.getKs(), SimpInterpreter.getP(), p1);
            LightingCalculation lightObjP2 = new LightingCalculation(SimpInterpreter.getLightColor(), SimpInterpreter.getAttA(),
                    SimpInterpreter.getAttB(), SimpInterpreter.getAmbientLight(),
                    SimpInterpreter.getKd(), SimpInterpreter.getLightLocation(), normal_p2, SimpInterpreter.getKs(), SimpInterpreter.getP(), p2);
            LightingCalculation lightObjP3 = new LightingCalculation(SimpInterpreter.getLightColor(), SimpInterpreter.getAttA(),
                    SimpInterpreter.getAttB(), SimpInterpreter.getAmbientLight(),
                    SimpInterpreter.getKd(), SimpInterpreter.getLightLocation(), normal_p3, SimpInterpreter.getKs(), SimpInterpreter.getP(), p3);
            Color color1= lightObjP1.getLightingColor();
            Color color2 = lightObjP2.getLightingColor();
            Color color3 = lightObjP3.getLightingColor();

            p1Left = determinePoint(p1Left, p1, p2, p3, color1, color2, color3);
            p2Left = determinePoint(p2Left, p1, p2, p3, color1, color2, color3);
            p1Right = determinePoint(p1Right, p1, p2, p3, color1, color2, color3);
            p2Right = determinePoint(p2Right, p1, p2, p3, color1, color2, color3);
        } else {             // PHONG
            Vector normal_p3 = null;
            Vector normal_p2 = null;
            Vector normal_p1 = null;

            double p1Left_y = p1Left.getY();
            double p2Left_y = p2Left.getY();
            boolean sign = true;

            if (SimpInterpreter.isHasNormal()) {
                normal_p1 = p1.getNormal().normalize();
                normal_p2 = p2.getNormal().normalize();
                normal_p3 = p3.getNormal().normalize();
            } else {
                normal_p3 = new Vector(p3.getPoint3D(), p1.getPoint3D()).crossProduct(new Vector(p3.getPoint3D(),
                        p2.getPoint3D())).normalize();
                normal_p2 = new Vector(p2.getPoint3D(), p3.getPoint3D()).crossProduct(new Vector(p2.getPoint3D(),
                        p1.getPoint3D())).normalize();
                normal_p1 = new Vector(p1.getPoint3D(), p2.getPoint3D()).crossProduct(new Vector(p1.getPoint3D(),
                        p3.getPoint3D())).normalize();
            }
            LightingCalculation lightObjP1 = new LightingCalculation(SimpInterpreter.getLightColor(), SimpInterpreter.getAttA(), SimpInterpreter.getAttB(), SimpInterpreter.getAmbientLight(),
                    SimpInterpreter.getKd(), SimpInterpreter.getLightLocation(),
                    normal_p1, SimpInterpreter.getKs(), SimpInterpreter.getP(), p1);
            LightingCalculation lightObjP2 = new LightingCalculation(SimpInterpreter.getLightColor(), SimpInterpreter.getAttA(), SimpInterpreter.getAttB(), SimpInterpreter.getAmbientLight(),
                    SimpInterpreter.getKd(), SimpInterpreter.getLightLocation(),
                    normal_p2, SimpInterpreter.getKs(), SimpInterpreter.getP(), p2);
            LightingCalculation lightObjP3 = new LightingCalculation(SimpInterpreter.getLightColor(), SimpInterpreter.getAttA(), SimpInterpreter.getAttB(), SimpInterpreter.getAmbientLight(),
                    SimpInterpreter.getKd(), SimpInterpreter.getLightLocation(),
                    normal_p3, SimpInterpreter.getKs(), SimpInterpreter.getP(), p3);
            Color color1 = lightObjP1.getLightingColor();
            Color color2 = lightObjP2.getLightingColor();
            Color color3 = lightObjP3.getLightingColor();
            p1Left = determinePoint(p1Left, p1, p2, p3, color1, color2, color3);
            p2Left = determinePoint(p2Left, p1, p2, p3, color1, color2, color3);
            p1Right = determinePoint(p1Right, p1, p2, p3, color1, color2, color3);
            p2Right = determinePoint(p2Right, p1, p2, p3, color1, color2, color3);

            Vertex3D normalCopiedP1L = null;
            Vertex3D normalCopiedp1R = null;
            Vertex3D normalCopiedP2L = null;
            Vertex3D normalCopiedp2R = null;
            normalCopiedP1L = determinePointPhong(p1Left, normalCopiedP1L, p1, p2, p3, color1, color2, color3, normal_p1, normal_p2, normal_p3);
            normalCopiedP2L = determinePointPhong(p2Left, normalCopiedP2L, p1, p2, p3, color1, color2, color3, normal_p1, normal_p2, normal_p3);
            normalCopiedp1R = determinePointPhong(p1Right, normalCopiedp1R, p1, p2, p3, color1, color2, color3, normal_p1, normal_p2, normal_p3);
            normalCopiedp2R = determinePointPhong(p2Right, normalCopiedp2R, p1, p2, p3, color1, color2, color3, normal_p1, normal_p2, normal_p3);

            PointInterpolation xLeft = new PointInterpolation(p1Left_y, p2Left_y,
                    originalYValue, p1Left.getX(), p2Left.getX(), sign);
            PointInterpolation xRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                    originalYValue, p1Right.getX(), p2Right.getX(), sign);

            PointInterpolation zLeft = new PointInterpolation(p1Left_y, p2Left_y,
                    originalYValue, 1 / p1Left.getZ(), 1 / p2Left.getZ(), sign);
            PointInterpolation zRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                    originalYValue, 1 / p1Right.getZ(), 1 / p2Right.getZ(), sign);

            PointInterpolation redLeft = new PointInterpolation(p1Left_y, p2Left_y,
                    originalYValue, p1Left.getColor().getR() / p1Left.getZ(), p2Left.getColor().getR() / p2Left.getZ(), sign);
            PointInterpolation redRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                    originalYValue, p1Right.getColor().getR() / p1Right.getZ(), p2Right.getColor().getR() / p2Right.getZ(), sign);

            PointInterpolation greenLeft = new PointInterpolation(p1Left_y, p2Left_y,
                    originalYValue, p1Left.getColor().getG() / p1Left.getZ(), p2Left.getColor().getG() / p2Left.getZ(), sign);
            PointInterpolation greenRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                    originalYValue, p1Right.getColor().getG() / p1Right.getZ(), p2Right.getColor().getG() / p2Right.getZ(), sign);

            PointInterpolation blueLeft = new PointInterpolation(p1Left_y, p2Left_y,
                    originalYValue, p1Left.getColor().getB() / p1Left.getZ(), p2Left.getColor().getB() / p2Left.getZ(), sign);
            PointInterpolation blueRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                    originalYValue, p1Right.getColor().getB() / p1Right.getZ(), p2Right.getColor().getB() / p2Right.getZ(), sign);

            PointInterpolation xNormalLeft = new PointInterpolation(p1Left_y, p2Left_y,
                    originalYValue, normalCopiedP1L.getNormal().getX(), normalCopiedP2L.getNormal().getX(), sign);
            PointInterpolation xNormalRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                    originalYValue, normalCopiedp1R.getNormal().getX(), normalCopiedp2R.getNormal().getX(), sign);
            PointInterpolation zNormalLeft = new PointInterpolation(p1Left_y, p2Left_y,
                    originalYValue, normalCopiedP1L.getNormal().getZ(), normalCopiedP2L.getNormal().getZ(), sign);
            PointInterpolation zNormalRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                    originalYValue, normalCopiedp1R.getNormal().getZ(), normalCopiedp2R.getNormal().getZ(), sign);
            PointInterpolation yNormalLeft = new PointInterpolation(p1Left_y, p2Left_y,
                    originalYValue, normalCopiedP1L.getNormal().getY(), normalCopiedP2L.getNormal().getY(), sign);
            PointInterpolation yNormalRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                    originalYValue, normalCopiedp1R.getNormal().getY(), normalCopiedp2R.getNormal().getY(), sign);

            PointInterpolation xCamLeft = new PointInterpolation(p1Left_y, p2Left_y,
                    originalYValue, normalCopiedP1L.getX(), normalCopiedP2L.getX(), sign);
            PointInterpolation xCamRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                    originalYValue, normalCopiedp1R.getX(), normalCopiedp2R.getX(), sign);
            PointInterpolation yCamLeft = new PointInterpolation(p1Left_y, p2Left_y,
                    originalYValue, normalCopiedP1L.getY(), normalCopiedP2L.getY(), sign);
            PointInterpolation yCamRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                    originalYValue, normalCopiedp1R.getY(), normalCopiedp2R.getY(), sign);
            PointInterpolation zCamLeft = new PointInterpolation(p1Left_y, p2Left_y,
                    originalYValue, normalCopiedP1L.getZ(), normalCopiedP2L.getZ(), sign);
            PointInterpolation zCamRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                    originalYValue, normalCopiedp1R.getZ(), normalCopiedp2R.getZ(), sign);

            for (double y = originalYValue; y > endY; y--) {
                double xRightValue = xRight.getValue();
                PointInterpolation innerZ = new PointInterpolation(xLeft.getValue(), xRightValue,
                        xLeft.getValue(), zLeft.getValue(), zRight.getValue());
                PointInterpolation innerRed = new PointInterpolation(xLeft.getValue(), xRightValue,
                        xLeft.getValue(), redLeft.getValue(), redRight.getValue());
                PointInterpolation innerGreen = new PointInterpolation(xLeft.getValue(), xRightValue,
                        xLeft.getValue(), greenLeft.getValue(), greenRight.getValue());
                PointInterpolation innerBlue = new PointInterpolation(xLeft.getValue(), xRightValue,
                        xLeft.getValue(), blueLeft.getValue(), blueRight.getValue());

                PointInterpolation innerCamX = new PointInterpolation(xLeft.getValue(), xRightValue,
                        xLeft.getValue(), xCamLeft.getValue(), xCamRight.getValue());
                PointInterpolation innerCamY = new PointInterpolation(xLeft.getValue(), xRightValue,
                        xLeft.getValue(), yCamLeft.getValue(), yCamRight.getValue());
                PointInterpolation innerCamZ = new PointInterpolation(xLeft.getValue(), xRightValue,
                        xLeft.getValue(), zCamLeft.getValue(), zCamRight.getValue());
                PointInterpolation innerNormalX = new PointInterpolation(xLeft.getValue(), xRightValue,
                        xLeft.getValue(), xNormalLeft.getValue(), xNormalRight.getValue());
                PointInterpolation innerNormalY = new PointInterpolation(xLeft.getValue(), xRightValue,
                        xLeft.getValue(), yNormalLeft.getValue(), yNormalRight.getValue());
                PointInterpolation innerNormalZ = new PointInterpolation(xLeft.getValue(), xRightValue,
                        xLeft.getValue(), zNormalLeft.getValue(), zNormalRight.getValue());


                for (double x = xLeft.getValue(); x < xRightValue; x++) { // for each line, from left to right
                    double realZ = 1 / innerZ.getValue();
                    double realRed = realZ * innerRed.getValue();
                    double realGreen = realZ * innerGreen.getValue();
                    double realBlue = realZ * innerBlue.getValue();

                    double innerCamXValue = innerCamX.getValue();
                    double innerCamYValue = innerCamY.getValue();
                    double innerCamZValue = innerCamZ.getValue();
                    double innerNormalXValue = innerNormalX.getValue();
                    double innerNormalYValue = innerNormalY.getValue();
                    double innerNormalZValue = innerNormalZ.getValue();

                    Color color = new Color(realRed, realGreen, realBlue);

                    Vertex3D d = new Vertex3D(innerCamXValue, innerCamYValue, innerCamZValue, color);
                    Vector normal = new Vector(innerNormalXValue, innerNormalYValue, innerNormalZValue);
                    LightingCalculation light_obj = new LightingCalculation(SimpInterpreter.getLightColor(), SimpInterpreter.getAttA(),
                            SimpInterpreter.getAttB(), SimpInterpreter.getAmbientLight(),
                            SimpInterpreter.getKd(), SimpInterpreter.getLightLocation(), normal, SimpInterpreter.getKs(), SimpInterpreter.getP(), d);
                    Color c = light_obj.getLightingColor();

                    if (y >= 0 && y < 650 && x < 650 && x >= 0 && realZ >= SimpInterpreter.getYon() && realZ <= SimpInterpreter.getHeight()) {
                        if (SimpInterpreter.zBuffering.getValue((int) x, (int) y) < realZ) {
                            panel.setPixel((int) x, (int) y, realZ, c.asARGB());
                            SimpInterpreter.zBuffering.changeValue((int) x, (int) y, realZ);
                        }
                    }
                    innerZ.add();
                    innerRed.add();innerGreen.add();innerBlue.add();

                    innerCamX.add();innerCamY.add();innerCamZ.add();
                    innerNormalX.add();innerNormalY.add();innerNormalZ.add();
                }

                xLeft.add();xRight.add();
                zLeft.add();zRight.add();
                redLeft.add();redRight.add();
                greenLeft.add();greenRight.add();
                blueLeft.add();blueRight.add();

                xNormalLeft.add();xNormalRight.add();
                zNormalLeft.add();zNormalRight.add();
                yNormalLeft.add();yNormalRight.add();

                xCamLeft.add();xCamRight.add();
                yCamLeft.add();yCamRight.add();
                zCamLeft.add();zCamRight.add();
            }

            if (p2Left.getY() == endY) {
                if (p2Right.getY() == endY) {
                    return SIDE.BOTH;
                } else {
                    return SIDE.LEFT;
                }
            } else {
                return SIDE.RIGHT;
            }
        }
        double p1Left_y = p1Left.getY();
        double p2Left_y = p2Left.getY();
        boolean sign = true;

        PointInterpolation xLeft = new PointInterpolation(p1Left_y, p2Left_y,
                originalYValue, p1Left.getX(), p2Left.getX(), sign);
        PointInterpolation xRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                originalYValue, p1Right.getX(), p2Right.getX(), sign);

        PointInterpolation zLeft = new PointInterpolation(p1Left_y, p2Left_y,
                originalYValue, 1 / p1Left.getZ(), 1 / p2Left.getZ(), sign);
        PointInterpolation zRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                originalYValue, 1 / p1Right.getZ(), 1 / p2Right.getZ(), sign);

        PointInterpolation redLeft = new PointInterpolation(p1Left_y, p2Left_y,
                originalYValue, p1Left.getColor().getR() / p1Left.getZ(), p2Left.getColor().getR() / p2Left.getZ(), sign);
        PointInterpolation redRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                originalYValue, p1Right.getColor().getR() / p1Right.getZ(), p2Right.getColor().getR() / p2Right.getZ(), sign);

        PointInterpolation greenLeft = new PointInterpolation(p1Left_y, p2Left_y,
                originalYValue, p1Left.getColor().getG() / p1Left.getZ(), p2Left.getColor().getG() / p2Left.getZ(), sign);
        PointInterpolation greenRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                originalYValue, p1Right.getColor().getG() / p1Right.getZ(), p2Right.getColor().getG() / p2Right.getZ(), sign);

        PointInterpolation blueLeft = new PointInterpolation(p1Left_y, p2Left_y,
                originalYValue, p1Left.getColor().getB() / p1Left.getZ(), p2Left.getColor().getB() / p2Left.getZ(), sign);
        PointInterpolation blueRight = new PointInterpolation(p1Right.getY(), p2Right.getY(),
                originalYValue, p1Right.getColor().getB() / p1Right.getZ(), p2Right.getColor().getB() / p2Right.getZ(), sign);

        for (double y = originalYValue; y > endY; y--){
            double xRightValue = xRight.getValue();
            double xLeftValue = xLeft.getValue();

            PointInterpolation innerZ = new PointInterpolation(xLeftValue,xRightValue,
                    xLeftValue, zLeft.getValue(), zRight.getValue());
            PointInterpolation innerRed = new PointInterpolation(xLeftValue,xRightValue,
                    xLeftValue, redLeft.getValue(), redRight.getValue());
            PointInterpolation innerGreen = new PointInterpolation(xLeftValue,xRightValue,
                    xLeftValue, greenLeft.getValue(), greenRight.getValue());
            PointInterpolation innerBlue = new PointInterpolation(xLeftValue,xRightValue,
                    xLeftValue, blueLeft.getValue(), blueRight.getValue());

            for (double x = xLeftValue; x < xRightValue; x++){
                double realZ = 1 / innerZ.getValue();
                double realRed = realZ * innerRed.getValue();
                double realGreen = realZ * innerGreen.getValue();
                double realBlue = realZ * innerBlue.getValue();

                Color color = new Color(realRed, realGreen, realBlue);
                if(y >= 0 && y < 650 && x < 650 && x >= 0 && realZ >= SimpInterpreter.getYon() && realZ <= SimpInterpreter.getHeight()) {
                    if (SimpInterpreter.zBuffering.getValue((int) x, (int) y) < realZ) {
                        panel.setPixel((int) x, (int) y, realZ, color.asARGB());
                        SimpInterpreter.zBuffering.changeValue((int) x, (int) y, realZ);
                    }
                }
                innerZ.add();
                innerRed.add();innerGreen.add();innerBlue.add();
            }

            xLeft.add(); xRight.add();
            zLeft.add(); zRight.add();
            redLeft.add(); redRight.add();
            greenLeft.add(); greenRight.add();
            blueLeft.add(); blueRight.add();
        }

        if (p2Left.getY() == endY){
            if (p2Right.getY() == endY){return SIDE.BOTH;}
            else {return SIDE.LEFT;}
        } else {
            return SIDE.RIGHT;
        }
    }

    private Vertex3D determinePoint(Vertex3D point, Vertex3D p1, Vertex3D p2, Vertex3D p3, Color c1, Color c2, Color c3) {
        Vertex3D p1L = point;
        if (p1L.isEqual(p1)) { p1L = p1L.replaceColor(c1);
        } else if (p1L.isEqual(p2)) { p1L = p1L.replaceColor(c2);
        } else if (p1L.isEqual(p3)) { p1L = p1L.replaceColor(c3);
        }
        return p1L;
    }

    private Vertex3D determinePointPhong(Vertex3D point, Vertex3D matchPoint, Vertex3D p1, Vertex3D p2, Vertex3D p3,
                                         Color c1, Color c2, Color c3, Vector normalP1, Vector normalP2, Vector normalP3){
        Vertex3D p1L = point;
        if(p1L.isEqual(p1)){
            p1L = p1L.replaceColor(c1);
            matchPoint = p1;
            matchPoint = matchPoint.setNormal(normalP1);
        }else if(p1L.isEqual(p2)){
            p1L = p1L.replaceColor(c2);
            matchPoint = p2;
            matchPoint = matchPoint.setNormal(normalP2);
        }else{
            p1L = p1L.replaceColor(c3);
            matchPoint = p3;
            matchPoint = matchPoint.setNormal(normalP3);
        }
        return matchPoint;
    }
}