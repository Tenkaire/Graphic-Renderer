package client.interpreter;

import geometry.Point3D;
import geometry.Point3DH;
import geometry.Transformation;
import geometry.Vector;

public class MathCalculation {
    public MathCalculation(){}

    public static Vector normalize(Vector N1, Vector N2, Vector N3){   //flat shading lighting calculation
        double normalX = (N1.getX()+N2.getX()+N3.getX())/3;
        double normalY = (N1.getY()+N2.getY()+N3.getY())/3;
        double normalZ = (N1.getZ()+N2.getZ()+N3.getZ())/3;
        return new Vector(normalX,normalY,normalZ);
    }

    public static Vector normalize(Point3DH V1, Point3DH V2, Point3DH V3){
        Point3DH tempV1 = V2.subtract(V1);
        Point3DH tempV2 = V3.subtract(V1);
        return crossProduct(tempV1,tempV2);
    }

    public static Vector crossProduct (Point3DH V1, Point3DH V2){
        double vectorX = V1.getY()*V2.getZ() - V1.getZ()*V2.getY();
        double vectorY = V1.getZ()*V2.getX() - V1.getX()*V2.getZ();
        double vectorZ = V1.getX()*V2.getY() - V1.getY()*V2.getX();
        return new Vector(vectorX,vectorY,vectorZ);
    }

    public static double dotProduct (Vector V1, Vector V2){
        double tempX = V1.getX()*V2.getX();
        double tempY = V1.getY()*V2.getY();
        double tempZ = V1.getZ()*V2.getZ();
        return (tempX+tempY+tempZ);
    }

    public static double exponent(double num, int base){
        double total = 1;
        for(int i = 0; i < base; i++){
            total *= num;
        }
        return total;
    }

    public static double[][] multiply(double[][] ctm, Transformation sk) {
        double[][] temp = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < sk.getWidth(); j++) {
                double lineTotal = 0;
                for (int index = 0; index < 4; index++) {
                    lineTotal += ctm[i][index] * sk.getValue(index, j);
                }
                temp[i][j] = lineTotal;
            }
        }
        return temp;
    }

}
