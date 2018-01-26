package geometry;

import client.interpreter.SimpInterpreter;
import windowing.graphics.Color;

public class Vertex3D implements Vertex {
	protected Point3DH point;
	protected Color color;
	protected Point3DH point_screen_space;
	protected Vector normal;
	
	public Vertex3D(Point3DH point, Color color) {
		super();
		this.point = point;
		this.color = color;
		this.point_screen_space = applyNonSquareMatrix(applyViewToScreenMatrix(applyPerspective(this.point)));
	}
	public Vertex3D(double x, double y, double z, Color color) {
		this(new Point3DH(x, y, z), color);
		this.point_screen_space = applyNonSquareMatrix(applyViewToScreenMatrix(applyPerspective(this.point)));
	}

	public Vertex3D(Point3DH point, Color color, Vector v){
	    super();
	    this.point = point;
	    this.color = color;
	    this.normal = v;
	    this.point_screen_space = applyNonSquareMatrix(applyViewToScreenMatrix(applyPerspective(this.point)));
    }

	public Vertex3D() {
	}
	public double getX() {
		return point.getX();
	}
	public double getY() {
		return point.getY();
	}
	public double getZ() {
		return point.getZ();
	}
	public double getCameraSpaceZ() {
		return getZ();
	}
	public Point getPoint() {
		return point;
	}
//	public Point3DH getPoint3D() {
//		return point;
//	}
	public Point3D getPoint3D() {
		return new Point3D(point.getX(), point.getY(), point.getZ(), point.getW());
	}
	
	public int getIntX() {
		return (int) Math.round(getX());
	}
	public int getIntY() {
		return (int) Math.round(getY());
	}
	public int getIntZ() {
		return (int) Math.round(getZ());
	}
	
	public Color getColor() {
		return color;
	}
	
	public Vertex3D rounded() {
		return new Vertex3D(point.round(), color);
	}
	public Vertex3D add(Vertex other) {
		Vertex3D other3D = (Vertex3D)other;
		return new Vertex3D(point.add(other3D.getPoint()),
				            color.add(other3D.getColor()));
	}
	public Vertex3D subtract(Vertex other) {
		Vertex3D other3D = (Vertex3D)other;
		return new Vertex3D(point.subtract(other3D.getPoint()),
				            color.subtract(other3D.getColor()));
	}
	public Vertex3D scale(double scalar) {
		return new Vertex3D(point.scale(scalar),
				            color.scale(scalar));
	}
	public Vertex3D replacePoint(Point3DH newPoint) {
		return new Vertex3D(newPoint, color);
	}
	public Vertex3D replacePoint(Point3D newPoint, Color this_color) {return new Vertex3D(newPoint.getX(), newPoint.getY(), newPoint.getZ(), this_color);}
	public Vertex3D replaceColor(Color newColor) {
		return new Vertex3D(point, newColor);
	}
	public Vertex3D replaceColor(Vertex3D point, Color newColor) {
//	    System.out.println("----------------------");
//	    System.out.println(newColor);
	    double x = point.getX();
	    double y = point.getY();
	    double z = point.getZ();
	    Vertex3D temp =  new Vertex3D(new Point3DH(x, y ,z), newColor);
//	    System.out.println(temp);
	    return temp;
    }
	public Vertex3D euclidean() {
		Point3D euclidean = getPoint3D();
		return replacePoint(euclidean, color);
	}
	
	public String toString() {
		return "(" + getX() + ", " + getY() + ", " + getZ() + ", " + getColor().toIntString() + ")";
	}
	public String toIntString() {
		return "(" + getIntX() + ", " + getIntY() + getIntZ() + ", " + ", " + getColor().toIntString() + ")";
	}
	public Vertex3D addVector(Vector v){
		Point3D p = point.addVector(v);
		Color this_color = color;
		return replacePoint(p, this_color);
	}

	public void setZ(double new_Z) {
		this.point.setZ(new_Z);
	}

	public boolean isEqual(Vertex3D cam_point){
		if(cam_point.point_screen_space.getX() == this.point.getX() && cam_point.point_screen_space.getY() == this.point.getY()
				&& cam_point.point_screen_space.getZ() == this.point.getZ())
			return true;
		return false;
	}

	private Point3DH applyPerspective(Point3DH points){
		double[][] perspective_mat;
		double z = points.getZ();
		perspective_mat = SimpInterpreter.getPerspectiveMatrix();
		double[] temp = new double[4];
		double[] current_point = new double[4];
		current_point[0] = points.getX();
		current_point[1] = points.getY();
		current_point[2] = points.getZ();
		current_point[3] = 1;
		for(int i = 0; i < 4; i++) {
			double lineTotal = 0;
			for(int j = 0; j < 4; j++) {
				lineTotal += perspective_mat[i][j] * current_point[j];
			}
			temp[i] = lineTotal;
		}
		if(temp[3] == 0)
			temp[3] = 1;
		Point3DH ret = new Point3DH(temp).euclidean();
		ret.setZ(z);
		return ret;
	}

	private Point3DH applyViewToScreenMatrix(Point3DH points){
		double[][] viewToScreen;
		double z = points.getZ();
		viewToScreen = SimpInterpreter.getViewToScreen();
		double[] temp = new double[4];
		double[] current_point = new double[4];
		current_point[0] = points.getX();
		current_point[1] = points.getY();
		current_point[2] = points.getZ();
		current_point[3] = 1;
		for(int i = 0; i < 4; i++) {
			double lineTotal = 0;
			for(int j = 0; j < 4; j++) {
				lineTotal += viewToScreen[i][j] * current_point[j];
			}
			temp[i] = lineTotal;
		}
		if(temp[3] == 0)
			temp[3] = 1;
		Point3DH ret = new Point3DH(temp).euclidean();
		ret.setZ(z);
		return ret;
	}

	private Point3DH applyNonSquareMatrix(Point3DH points){
		double[][] nonSquareMatrix;
		double z = points.getZ();
		nonSquareMatrix = SimpInterpreter.get_non_square_matrix();
		double[] temp = new double[4];
		double[] current_point = new double[4];
		current_point[0] = points.getX();
		current_point[1] = points.getY();
		current_point[2] = points.getZ();
		current_point[3] = 1;
		for(int i = 0; i < 4; i++) {
			double lineTotal = 0;
			for(int j = 0; j < 4; j++) {
				lineTotal += nonSquareMatrix[i][j] * current_point[j];
			}
			temp[i] = lineTotal;
		}
		if(temp[3] == 0)
			temp[3] = 1;
		Point3DH ret = new Point3DH(temp).euclidean();
		ret.setZ(z);
		return ret;
	}

	public Vector getNormal(){return normal;}

	public Vertex3D setNormal(Vector newVector){
		return new Vertex3D(this.point, this.color, newVector);
	}

}
