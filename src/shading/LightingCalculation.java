package shading;

import client.interpreter.MathCalculation;
import geometry.Point3D;
import geometry.Vector;
import geometry.Vertex3D;
import windowing.graphics.Color;

import java.util.ArrayList;

public class LightingCalculation {
    private ArrayList<Color> intensity; // light
    private ArrayList<Double> A;
    private ArrayList<Double> B;
    private Color ambientLight;
    private Color kd;
    private ArrayList<Point3D> light_location;
    private Vector normal;
    private double ks;
    private double p;
    private Vertex3D point;

    private enum intensityColor{RED,GREEN,BLUE}

    public LightingCalculation(ArrayList<Color> intensity, ArrayList<Double> a, ArrayList<Double> b, Color ambientLight, Color kd,
                               ArrayList<Point3D> light_location, Vector normal, double ks, double p, Vertex3D point) {
        this.intensity = intensity;
        A = a;
        B = b;
        this.ambientLight = ambientLight;
        this.kd = kd;
        this.light_location = light_location;
        this.normal = normal;
        this.ks = ks;
        this.p = p;
        this.point = point;
    }

    public Color getLightingColor(){
        double r = getColor(intensityColor.RED);
        double g = getColor(intensityColor.GREEN);
        double b = getColor(intensityColor.BLUE);

//        System.out.println("RGB = " + r + " " + g + " " + b);
//        System.out.println("=============================================");
        return Color.fromARGB(Color.makeARGB((int)Math.round(r*255), (int)Math.round(g*255), (int)Math.round(b*255)));
    }


    private double getColor(intensityColor intensity_color){
        double color;
        ArrayList<Vector> L, V, R;
        L = new ArrayList<>();
        V = new ArrayList<>();
        R = new ArrayList<>();
        ArrayList<Double> constant = new ArrayList<>();
        getConsistant(L, V, R,  constant);
        if(intensity_color == intensityColor.RED){
            color = kd.getR() * ambientLight.getR();
            for(int i = 0; i < intensity.size(); i++){
                color += intensity.get(i).getR() * (1/(A.get(i)+B.get(i)*getDistance(i))) *
                        (kd.getR()*(normal.normalize().dotProduct(L.get(i).normalize())) + constant.get(i));
            }
        }else if(intensity_color == intensityColor.GREEN){
            color = kd.getG() * ambientLight.getG();
            for(int i = 0; i < intensity.size(); i++){
                color += intensity.get(i).getG() * (1/(A.get(i)+B.get(i)*getDistance(i))) *
                        (kd.getG()*(normal.normalize().dotProduct(L.get(i).normalize())) + constant.get(i));
            }
        }else{
            color = kd.getB() * ambientLight.getB();
            for(int i = 0; i < intensity.size(); i++){
                color += intensity.get(i).getB() * (1/(A.get(i)+B.get(i)*getDistance(i))) *
                        (kd.getB()*(normal.normalize().dotProduct(L.get(i).normalize())) + constant.get(i));
            }
        }

        if(color > 1) color = 1;
        if(color < 0) color = 0;
        return color;
    }

    private double getDistance(int i) {
        double distance;
        double dis_X =  light_location.get(i).getX() - point.getX();
        double dis_Y =  light_location.get(i).getY() - point.getY();
        double dis_Z =  light_location.get(i).getZ() - point.getZ();
        distance = Math.sqrt(Math.pow(dis_X, 2) + Math.pow(dis_Y, 2) + Math.pow(dis_Z, 2));
        return distance;
    }

    private void getConsistant(ArrayList<Vector> L, ArrayList<Vector> V, ArrayList<Vector> R, ArrayList<Double> constant){
        for(int i = 0; i < light_location.size(); i++) {
            L.add(new Vector(point.getPoint3D(), light_location.get(i)).normalize());
            Vector temp_L = L.get(L.size()-1);
            V.add(new Vector(new Point3D(0, 0, 0), point.getPoint3D()).normalize());
            Vector temp_V = V.get(V.size()-1);
            Vector temp_vector = normal.scale(normal.dotProduct(temp_L) * 2).subtract(temp_L).normalize();
            R.add(temp_vector);
            Vector temp_R = R.get(R.size()-1);
            constant.add(ks * Math.pow(temp_V.normalize().dotProduct(temp_R.normalize()), p));
        }
    }

}
