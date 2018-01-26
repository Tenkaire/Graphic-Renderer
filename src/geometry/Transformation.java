package geometry;

import client.interpreter.MatrixCalculation;

public class Transformation {
    private static double[][] matrix;

    public Transformation() {
        matrix = new double[4][4];
    }

    public static Transformation Identity(){
        Transformation trans = new Transformation();
        matrix = MatrixCalculation.identity();
        return trans;
    }

    public static void alterValue(int row, int col, double value) {
        matrix[row][col] = value;
    }

    public double getValue(int row, int col) {
        return matrix[row][col];
    }

    public int getWidth(){return 4;}
    public int getHeight() {return 4;}

    public double[][] getMatrix(){return matrix;}
}
